import {InputItem, ItemConfig} from "@/components/form/config";
import ObjectRelAddView from "@/components/entity/object/ObjectRelAddView.vue";

export class ObjectRelItem extends InputItem {


    constructor(code: string, label: string, prop: ItemConfig<ObjectRelItem> = {} as ItemConfig<ObjectRelItem>) {
        super(code, label, prop);
        this.prop.value = [];
        this.comp = ObjectRelAddView;
    }

}