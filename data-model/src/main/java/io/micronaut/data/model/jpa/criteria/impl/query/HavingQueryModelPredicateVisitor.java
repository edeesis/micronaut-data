package io.micronaut.data.model.jpa.criteria.impl.query;

import io.micronaut.data.model.query.QueryModel;

public class HavingQueryModelPredicateVisitor extends QueryModelPredicateVisitor {
    public HavingQueryModelPredicateVisitor(QueryModel queryModel) {
        super(queryModel);
    }

    @Override
    protected void add(QueryModel.Criterion criterion) {
        if (state.negated) {
            QueryModel.Negation negation = new QueryModel.Negation();
            negation.add(criterion);
            criterion = negation;
        }
        if (state.junction == null) {
            queryModel.addHaving(criterion);
        } else {
            state.junction.add(criterion);
        }
    }
}
