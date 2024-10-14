package io.cockroachdb.wan.util;

import org.springframework.context.ApplicationEvent;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

public final class GenericEvent<T> extends ApplicationEvent implements ResolvableTypeProvider {
    public static <T> GenericEvent<T> of(Object source, T target) {
        return new GenericEvent<>(source, target);
    }

    private final T target;

    public GenericEvent(Object source, T target) {
        super(source);
        this.target = target;
    }

    public T getTarget() {
        return target;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(getClass(), ResolvableType.forInstance(target));
    }
}
