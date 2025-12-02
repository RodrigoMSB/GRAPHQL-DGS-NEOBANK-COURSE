package com.neobank.savings.scalar;

import com.netflix.graphql.dgs.DgsScalar;
import graphql.language.FloatValue;
import graphql.language.IntValue;
import graphql.schema.Coercing;
import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;

import java.math.BigDecimal;

@DgsScalar(name = "Money")
public class MoneyScalar implements Coercing<BigDecimal, Double> {
    
    @Override
    public Double serialize(Object dataFetcherResult) throws CoercingSerializeException {
        if (dataFetcherResult instanceof BigDecimal) {
            return ((BigDecimal) dataFetcherResult).doubleValue();
        }
        throw new CoercingSerializeException("Expected BigDecimal");
    }
    
    @Override
    public BigDecimal parseValue(Object input) throws CoercingParseValueException {
        try {
            return new BigDecimal(input.toString());
        } catch (Exception e) {
            throw new CoercingParseValueException("Invalid Money value");
        }
    }
    
    @Override
    public BigDecimal parseLiteral(Object input) throws CoercingParseLiteralException {
        if (input instanceof IntValue) {
            return new BigDecimal(((IntValue) input).getValue());
        } else if (input instanceof FloatValue) {
            return ((FloatValue) input).getValue();
        }
        throw new CoercingParseLiteralException("Expected number");
    }
}
