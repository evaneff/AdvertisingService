package com.amazon.ata.advertising.service.targeting;

import com.amazon.ata.advertising.service.model.RequestContext;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicate;
import com.amazon.ata.advertising.service.targeting.predicate.TargetingPredicateResult;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

/**
 * Evaluates TargetingPredicates for a given RequestContext.
 */
public class TargetingEvaluator {
    public static final boolean IMPLEMENTED_STREAMS = true;
    public static final boolean IMPLEMENTED_CONCURRENCY = true;
    private final RequestContext requestContext;


    /**
     * Creates an evaluator for targeting predicates.
     * @param requestContext Context that can be used to evaluate the predicates.
     */

    public TargetingEvaluator(RequestContext requestContext) {

        this.requestContext = requestContext;
    }

    /**

     * Evaluate a TargetingGroup to determine if all of its TargetingPredicates are TRUE or not for the given
     * RequestContext.
     * @param targetingGroup Targeting group for an advertisement, including TargetingPredicates.
     * @return TRUE if all of the TargetingPredicates evaluate to TRUE against the RequestContext, FALSE otherwise.
     */
    public TargetingPredicateResult evaluate(TargetingGroup targetingGroup) {

//        List<TargetingPredicate> targetingPredicates = targetingGroup.getTargetingPredicates();
//        List<Future<TargetingPredicateResult>> futureList = new ArrayList<>();
//        boolean allTruePredicates = true;
//        ExecutorService executor = Executors.newCachedThreadPool();
//        for (TargetingPredicate predicate : targetingPredicates) {
//            futureList.add(executor.submit(() -> predicate.evaluate(requestContext)));
//        }
//        executor.shutdown();
//
//        for (Future<TargetingPredicateResult> futureResult : futureList) {
//            try {
//                if (!futureResult.get().isTrue()) {
//                    allTruePredicates = false;
//                    break;
//                }
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            } catch (ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return allTruePredicates ? TargetingPredicateResult.TRUE :
//                TargetingPredicateResult.FALSE;
//    }
        ExecutorService executor = Executors.newCachedThreadPool();

//        boolean allTruePredicates = targetingGroup.getTargetingPredicates().stream()
//                .map(predicate -> executor.submit(() -> predicate.evaluate(requestContext)))
//                .allMatch(targetingPredicateResult -> targetingPredicateResult.isTrue());

        List<Future<TargetingPredicateResult>> futureList = targetingGroup.getTargetingPredicates().stream()
                .map(predicate -> executor.submit(() -> predicate.evaluate(requestContext)))
                .collect(Collectors.toList());

        executor.shutdown();

        boolean allTruePredicates = futureList.stream()
                .anyMatch(targetingPredicateResult -> {
                    try {
                        return targetingPredicateResult.get().isTrue();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                });

        return allTruePredicates ? TargetingPredicateResult.TRUE :
                TargetingPredicateResult.FALSE;
    }
}
