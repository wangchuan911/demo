import {InputCompLoadedHandler, InputItem} from "@/components/form/config";
import ObjectRelAddView from "@/components/entity/object/ObjectRelAddView.vue";

export class ObjectRelItem extends InputItem {


    constructor(code: string, label: string, prop: InputCompLoadedHandler<ObjectRelItem> = (input, content) => {
        console.log("empty function");
    }) {
        super(code, label, prop);
        this.prop.value = [];
        this.comp = ObjectRelAddView;
    }

}