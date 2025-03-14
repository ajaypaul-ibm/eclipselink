/*
 * Copyright (c) 1998, 2024 Oracle and/or its affiliates. All rights reserved.
 * Copyright (c) 2022 IBM Corporation. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0,
 * or the Eclipse Distribution License v. 1.0 which is available at
 * http://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: EPL-2.0 OR BSD-3-Clause
 */

// Contributors:
//     Oracle - initial API and implementation from Oracle TopLink
package org.eclipse.persistence.internal.expressions;

import org.eclipse.persistence.exceptions.QueryException;
import org.eclipse.persistence.expressions.Expression;
import org.eclipse.persistence.expressions.ExpressionBuilder;
import org.eclipse.persistence.expressions.ExpressionOperator;
import org.eclipse.persistence.history.AsOfClause;
import org.eclipse.persistence.internal.databaseaccess.DatabasePlatform;
import org.eclipse.persistence.internal.helper.DatabaseTable;
import org.eclipse.persistence.internal.helper.NonSynchronizedVector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Vector;

/**
 * Abstract class for expression that have exactly two children, such as and/or and relations.
 */
public abstract class CompoundExpression extends Expression {
    protected ExpressionOperator operator;
    protected transient ExpressionOperator platformOperator;
    protected Expression firstChild;
    protected Expression secondChild;
    protected ExpressionBuilder builder;

    protected CompoundExpression() {
        super();
    }

    /**
     * INTERNAL:
     * Return if the expression is equal to the other.
     * This is used to allow dynamic expression's SQL to be cached.
     */
    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        CompoundExpression expression = (CompoundExpression) object;
        return (Objects.equals(this.operator, expression.operator))
            && (Objects.equals(this.firstChild, expression.firstChild))
            && (Objects.equals(this.secondChild, expression.secondChild));
    }

    /**
     * INTERNAL:
     * Compute a consistent hash-code for the expression.
     * This is used to allow dynamic expression's SQL to be cached.
     */
    @Override
    public int computeHashCode() {
        int hashCode = super.computeHashCode();
        if (this.operator != null) {
            hashCode = hashCode + this.operator.hashCode();
        }
        if (this.firstChild != null) {
            hashCode = hashCode + this.firstChild.hashCode();
        }
        if (this.secondChild != null) {
            hashCode = hashCode + this.secondChild.hashCode();
        }
        return hashCode;
    }

    /**
     * INTERNAL:
     * Find the alias for a given table from the first or second child in the additionalOuterJoinCriteria
     */
    @Override
    public DatabaseTable aliasForTable(DatabaseTable table) {
        DatabaseTable alias = null;
        if (this.firstChild != null) {
            alias = this.firstChild.aliasForTable(table);
        }

        if ((alias == null) && (this.secondChild != null)) {
            alias = this.secondChild.aliasForTable(table);
        }

        return alias;
    }

    @Override
    public Expression asOf(AsOfClause clause) {
        final AsOfClause finalClause = clause;
        ExpressionIterator<Void> iterator = new ExpressionIterator<>() {
            @Override
            public void iterate(Expression each) {
                if (each.isDataExpression()) {
                    each.asOf(finalClause);
                }
            }

            @Override
            public boolean shouldIterateOverSubSelects() {
                return true;
            }
        };
        iterator.iterateOn(this);
        return this;
    }

    /**
     * INTERNAL:
     */
    @Override
    public Expression create(Expression base, Object singleArgument, ExpressionOperator operator) {
        setFirstChild(base);
        Expression argument = Expression.from(singleArgument, base);
        setSecondChild(argument);
        setOperator(operator);
        return this;
    }

    /**
     * INTERNAL:
     */
    @Override
    public Expression create(Expression base, List arguments, ExpressionOperator operator) {
        setFirstChild(base);
        if (!arguments.isEmpty()) {
            setSecondChild((Expression)arguments.get(0));
        }
        setOperator(operator);
        return this;
    }

    /**
     * INTERNAL:
     * Used for debug printing.
     */
    @Override
    public String descriptionOfNodeType() {
        return "Compound Expression";
    }

    /**
     * Return the expression builder which is the ultimate base of this expression, or
     * null if there isn't one (shouldn't happen if we start from a root)
     */
    @Override
    public ExpressionBuilder getBuilder() {
        // PERF: Cache builder.
        if (this.builder == null) {
            this.builder = this.firstChild.getBuilder();
            if (this.builder == null) {
                this.builder = this.secondChild.getBuilder();
            }
        }
        return this.builder;
    }

    public Expression getFirstChild() {
        return firstChild;
    }

    @Override
    public ExpressionOperator getOperator() {
        return operator;
    }

    public ExpressionOperator getPlatformOperator(DatabasePlatform platform) {
        if (platformOperator == null) {
            initializePlatformOperator(platform);
        }
        return platformOperator;
    }

    public Expression getSecondChild() {
        return secondChild;
    }

    /**
     * INTERNAL:
     */
    public void initializePlatformOperator(DatabasePlatform platform) {
        // First, check that the platform operator doesn't override the operator behavior
        platformOperator = platform.getOperator(this.operator.getSelector());
        if (platformOperator == null) {
            // If the platform doesn't specifically override, fallback on the internal operator
            // This operator should be either user-defined or one from ExpressionOperator.initializeInternalOperators.
            platformOperator = this.operator;
            if (platformOperator == null) {
                throw QueryException.invalidOperator(this.operator);
            }
        }
    }

    @Override
    public boolean isCompoundExpression() {
        return true;
    }

    /**
     * INTERNAL:
     * For iterating using an inner class
     */
    @Override
    public void iterateOn(ExpressionIterator iterator) {
        super.iterateOn(iterator);
        if (this.firstChild != null) {
            this.firstChild.iterateOn(iterator);
        }
        if (this.secondChild != null) {
            this.secondChild.iterateOn(iterator);
        }
    }

    /**
     * INTERNAL:
     * Normalize into a structure that is printable.
     * Also compute printing information such as outer joins.
     */
    @Override
    public Expression normalize(ExpressionNormalizer normalizer) {
        validateNode();
        boolean previous = normalizer.isAddAdditionalExpressionsWithinCurrrentExpressionContext();
        boolean isOrExpression = (isLogicalExpression() && this.operator.getSelector() == ExpressionOperator.Or);
        normalizer.setAddAdditionalExpressionsWithinCurrrentExpressionContext(previous|| isOrExpression);

        try {
            if (this.firstChild != null) {
                //let's make sure a session is available in the case of a parallel expression
                ExpressionBuilder builder = this.firstChild.getBuilder();
                if (builder != null){
                    builder.setSession(normalizer.getSession().getRootSession(null));
                }
                setFirstChild(normalizer.processAdditionalLocalExpressions(this.firstChild.normalize(normalizer), isOrExpression));
            }
            if (this.secondChild != null) {
                //let's make sure a session is available in the case of a parallel expression
                 ExpressionBuilder builder = this.secondChild.getBuilder();
                 if (builder != null){
                     builder.setSession(normalizer.getSession().getRootSession(null));
                 }
                setSecondChild(normalizer.processAdditionalLocalExpressions(this.secondChild.normalize(normalizer), isOrExpression));
            }
        } finally {
            normalizer.setAddAdditionalExpressionsWithinCurrrentExpressionContext(previous);
        }

        // For CR2456, it is now possible for normalize to remove redundant
        // conditions from the where clause.
        if (this.firstChild == null) {
            return this.secondChild;
        } else if (this.secondChild == null) {
            return this.firstChild;
        }
        return this;
    }

    /**
     * Do any required validation for this node. Throw an exception if it's incorrect.
     * Ensure that both sides are not data expressions.
     */
    @Override
    public void validateNode() {
        if (this.firstChild != null) {
            if (this.firstChild.isDataExpression() || this.firstChild.isConstantExpression()) {
                throw QueryException.invalidExpression(this);
            }
        }
        if (this.secondChild != null) {
            if (this.secondChild.isDataExpression() || this.secondChild.isConstantExpression()) {
                throw QueryException.invalidExpression(this);
            }
        }
    }

    /**
     * INTERNAL:
     * Used for cloning.
     */
    @Override
    protected void postCopyIn(Map alreadyDone) {
        super.postCopyIn(alreadyDone);
        if (this.firstChild != null) {
            setFirstChild(this.firstChild.copiedVersionFrom(alreadyDone));
        }
        if (this.secondChild != null) {
            setSecondChild(this.secondChild.copiedVersionFrom(alreadyDone));
        }
    }

    /**
     * INTERNAL:
     * Print SQL
     */
    @Override
    public void printSQL(ExpressionSQLPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        printer.printString("(");
        realOperator.printDuo(this.firstChild, this.secondChild, printer);
        printer.printString(")");
    }

    /**
     * INTERNAL:
     * Print java for project class generation
     */
    @Override
    public void printJava(ExpressionJavaPrinter printer) {
        ExpressionOperator realOperator = getPlatformOperator(printer.getPlatform());
        realOperator.printJavaDuo(this.firstChild, this.secondChild, printer);
    }

    /**
     * INTERNAL:
     * This expression is built on a different base than the one we want. Rebuild it and
     * return the root of the new tree
     */
    @Override
    public Expression rebuildOn(Expression newBase) {
        Vector arguments;

        Expression first = this.firstChild.rebuildOn(newBase);
        if (this.secondChild == null) {
            arguments = NonSynchronizedVector.newInstance(0);
        } else {
            arguments = NonSynchronizedVector.newInstance(1);
            arguments.add(this.secondChild.rebuildOn(newBase));
        }
        return first.performOperator(this.operator, arguments);
    }

    /**
     * INTERNAL:
     * Search the tree for any expressions (like SubSelectExpressions) that have been
     * built using a builder that is not attached to the query.  This happens in case of an Exists
     * call using a new ExpressionBuilder().  This builder needs to be replaced with one from the query.
     */
    @Override
    public void resetPlaceHolderBuilder(ExpressionBuilder queryBuilder){
        this.firstChild.resetPlaceHolderBuilder(queryBuilder);
        if (this.secondChild != null){
            this.secondChild.resetPlaceHolderBuilder(queryBuilder);
        }
    }

    protected void setFirstChild(Expression firstChild) {
        this.firstChild = firstChild;
        this.builder = null;
    }

    public void setOperator(ExpressionOperator newOperator) {
        operator = newOperator;
    }

    protected void setSecondChild(Expression secondChild) {
        this.secondChild = secondChild;
        this.builder = null;
    }

    /**
     * INTRENAL:
     * Used to change an expression off of one base to an expression off of a different base.
     * i.e. expression on address to an expression on an employee's address.
     */
    @Override
    public Expression twistedForBaseAndContext(Expression newBase, Expression context, Expression oldBase) {
        Vector arguments;

        if (this.secondChild == null) {
            arguments = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(0);
        } else {
            arguments = org.eclipse.persistence.internal.helper.NonSynchronizedVector.newInstance(1);
            arguments.addElement(this.secondChild.twistedForBaseAndContext(newBase, context, oldBase));
        }

        Expression first = this.firstChild.twistedForBaseAndContext(newBase, context, oldBase);
        return first.performOperator(this.operator, arguments);
    }

    /**
     * INTERNAL:
     * Used to print a debug form of the expression tree.
     */
    @Override
    public void writeDescriptionOn(BufferedWriter writer) throws IOException {
        writer.write(operator.toString());
    }

    /**
     * INTERNAL:
     * Used for toString for debugging only.
     */
    @Override
    public void writeSubexpressionsTo(BufferedWriter writer, int indent) throws IOException {
        if (this.firstChild != null) {
            this.firstChild.toString(writer, indent);
        }
        if (this.secondChild != null) {
            this.secondChild.toString(writer, indent);
        }
    }

    /**
     * INTERNAL:
     * Clear the builder when cloning.
     */
    @Override
    public Expression shallowClone() {
        CompoundExpression clone = (CompoundExpression)super.shallowClone();
        clone.builder = null;
        return clone;
    }
}
