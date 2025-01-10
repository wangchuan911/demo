package org.welisdoon.metadata.prototype.dao;

import org.welisdoon.metadata.prototype.define.MetaPrototype;

public interface MetaPrototypeCreator<T extends MetaPrototype> {
    T generate(long id);
}
