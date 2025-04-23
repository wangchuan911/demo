import {InputItem, ItemConfig} from "@/components/form/config";
import ObjectRelAddView from "@/components/entity/object/ObjectRelAddView.vue";
import AttrObjMapper from '@/components/form/input/AttrObjMapper.vue';

export class ObjectRelItem extends InputItem {


    constructor(code: string, label: string, prop: ItemConfig<ObjectRelItem> = {} as ItemConfig<ObjectRelItem>) {
        super(code, label, prop);
        this.prop.value = [];
        this.comp = ObjectRelAddView;
    }

}

export class AttrObjMapperItem extends InputItem {
    constructor(code: string, label: string, prop: ItemConfig<AttrObjMapperItem> = {} as ItemConfig<AttrObjMapperItem>) {
        super(code, label, prop);
        this.prop.value = {};
        this.prop.cols = [];
        this.prop.rows = [];
        this.prop.dels = [];
        this.prop.objectId = -1;
        this.comp = AttrObjMapper;
    }

}