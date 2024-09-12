export interface FormItemConfig {
    type: string
    placeholder?: string

    [key: string]: any
}

export class FormItemDefine {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    name!: string;
    id!: string;
    mode!: string;
    init?: any;
    config!: FormItemConfig;
}

import ObjectQueryInput from "@/components/form/input/ObjectQueryInput.vue";

export const InputModule = {
    ObjectQueryInput
};
import {ElInput} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';

export abstract class InputItem {
    code: string;
    label: string;
    comp: any;
    prop: any;

    constructor(code: string, label: string) {
        this.code = code;
        this.label = label;
        this.prop = {};
    }
}

export class Option {
    name: string;
    value: any;

    constructor(name: string, value: any) {
        this.name = name;
        this.value = value;
    }
}

export class selectItem extends InputItem {
    isMulti: boolean;
    checkBoxStyle: boolean;
    options: Array<Option>;

    constructor(code: string, label: string) {
        super(code, label);
        this.isMulti = false;
        this.checkBoxStyle = false;
        this.comp = MySelect;
        this.options = [];
    }

    setOptions(...options: Array<Option>): this {
        this.options.length = 0;
        this.addOptions(...options);
        return this;
    }

    addOptions(...options: Array<Option>): this {
        this.options.push(...options);
        return this;
    }

    setCheckBoxStyle(state: boolean): selectItem {
        this.checkBoxStyle = state;
        return this;
    }

    setMulti(state: boolean): selectItem {
        this.isMulti = state;
        return this;
    }
}

export enum TextType {
    TextArea, Default
}

export class TextItem extends InputItem {
    mode: TextType;

    constructor(code: string, label: string) {
        super(code, label);
        this.mode = TextType.Default;
        this.prop['type'] = 'text';
        this.comp = ElInput;
    }

    setMode(mode: TextType): TextItem {
        this.mode = mode;
        return this;
    }
}

export class QueryItem extends InputItem {
    constructor(code: string, label: string) {
        super(code, label);
        this.comp = ElInput;
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string) {
        super(code, label);
        this.isMulti = false;
        this.comp = ElInput;
    }

    setMulti(state: boolean): RadioItem {
        this.isMulti = state;
        return this;
    }
}


/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/

