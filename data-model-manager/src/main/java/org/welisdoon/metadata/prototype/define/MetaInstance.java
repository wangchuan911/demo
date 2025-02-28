package org.welisdoon.metadata.prototype.define;

/**
 * @Classname MetaInstance
 * @Description TODO
 * @Author Septem
 * @Date 15:09
 */
public class MetaInstance extends MetaPrototype {

    @Override
    public int remove() {
        if (getState() == LifeState.Delete)
            return 0;
        super.remove();
        setState(LifeState.Delete);
        return 0;
    }

    @Override
    public int save() {
        super.save();
        return 0;
    }

}
