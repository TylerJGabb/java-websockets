package com.gabb.sb;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static java.util.stream.Collectors.toList;

/**
 * A collection of objects that is thread safe, facilitating access to and modification of said objects
 * in a way that prevents those objects from being simultaneously mutated or accessed concurrently. In the end
 * it is up to the user to access these objects through this collection only, and not mutate them outside
 * of provided methods like {@link GuardedThreadSafeCollection#applyForEachAfterFiltering(Predicate, Function)} or
 * {@link GuardedThreadSafeCollection#findFirstAndConsume(Predicate, Consumer)}
 */
public abstract class GuardedThreadSafeCollection<G extends Guarded> {

    private final ReentrantLock oGuardedObjectsLock;
    private final List<G> oGuardedObjects;

    protected GuardedThreadSafeCollection() {
        oGuardedObjects = new ArrayList<>();
        oGuardedObjectsLock = new ReentrantLock(true);
    }

    protected void protectedAdd(G aGuarded) {
        try {
            oGuardedObjectsLock.lock();
            oGuardedObjects.add(aGuarded);
        } finally {
            oGuardedObjectsLock.unlock();
        }
    }

    protected void protectedRemove(G aGuarded) {
        try {
            oGuardedObjectsLock.lock();
            oGuardedObjects.remove(aGuarded);
        } finally {
            oGuardedObjectsLock.unlock();
        }
    }

    /**
     * Guards the provided {@link G} while testing it against the provided {@link Predicate}
     */
    private boolean guardedPredicateTester(G guarded, Predicate<G> aPredicate){
        try {
            guarded.guard();
            return aPredicate.test(guarded);
        } finally {
            guarded.relinquish();
        }
    }

    /**
     * Filters items in the collection according to aFilter, then applies the function aMutateThenReturnWhetherToStopIterating
     * to each item returned from the filer. If this method returns true, then iteration stops, otherwise iteration
     * will continue until exhausting all items in the filtered list.
     *
     * @param aFilter a filter that is applied to the current collection before beginning iteration
     * @param aMutateThenReturnWhetherToStopIterating a function that presumably mutates an object of type G and
     *                                                then returns true if iteration should stop
     */
    public void applyForEachAfterFiltering(
            Predicate<G> aFilter,
            Function<G, Boolean> aMutateThenReturnWhetherToStopIterating) {

        // FIRST: filter the collection according to the passed filter
        List<G> filtered;
        try {
            oGuardedObjectsLock.lock();
            filtered = oGuardedObjects.stream().filter(g -> guardedPredicateTester(g, aFilter)).collect(toList());
        } finally {
            oGuardedObjectsLock.unlock();
        }
        // SECOND: for each item in the filtered list, apply the provided function, stopping immediately
        // if said function returns true
        for (var guarded : filtered) {
            try {
                guarded.guard();
                Boolean stopIterating = aMutateThenReturnWhetherToStopIterating.apply(guarded);
                if(null != stopIterating && stopIterating) return;
            } finally {
                guarded.relinquish();
            }
        }
    }

    /**
     * Filters items in the collection according to aFilter, then accepts the provided aConsumer
     * for each item returned from the filer.
     *
     * @param aFilter the predicate used to filter the collection
     * @param aConsumer the consumer to invoke on each item returned from the filter
     */
    public void consumeForEachAfterFiltering(Predicate<G> aFilter, Consumer<G> aConsumer){
        applyForEachAfterFiltering(aFilter, g -> {
            aConsumer.accept(g);
            return false;
        });
    }

    public void consumeForEach(Consumer<G> aConsumer){
        List<G> listCopy;
        try {
            oGuardedObjectsLock.lock();
            listCopy = new ArrayList<>(oGuardedObjects);
        } finally {
            oGuardedObjectsLock.unlock();
        }
        for(var guarded: listCopy){
            try{
                guarded.guard();
                aConsumer.accept(guarded);
            } finally {
                guarded.relinquish();
            }
        }
    }

    /**
     * Finds the first {@link Guarded} that satisfies the provided predicate, and invokes the provided consumer
     * on it.
     */
    public void findFirstAndConsume(Predicate<G> aFilter, Consumer<G> aConsumer) {

        // first get a filtered list according to the filter
        G found;
        try {
            oGuardedObjectsLock.lock();
            found = oGuardedObjects.stream().filter(g -> guardedPredicateTester(g, aFilter)).findFirst().orElse(null);
        } finally {
            oGuardedObjectsLock.unlock();
        }
        if (found == null) return;

        // next accept consumer with found item, enforcing locks
        try {
            found.guard();
            aConsumer.accept(found);
        } finally {
            found.relinquish();
        }
    }
}
