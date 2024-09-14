

import {ElInput} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';

export abstract class InputItem {
    code: string;
    label: string;
    comp: any;
    prop: any;
    propAsync: any;
    groups: (() => Array<InputItem>);

    constructor(code: string, label: string, prop: any = {}) {
        this.code = code;
        this.label = label;
        this.prop = prop;
        this.groups = () => [];
    }

    load(propAsync: Record<string, (prop: any, groups: Array<InputItem>) => void>): this {
        Object.keys(propAsync).forEach(key => {
            this.prop[key] = propAsync[key](this.prop, this.groups());
        });
        return this;
    }

    setGroup(fun: () => Array<InputItem>) {
        this.groups = fun;
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

export class SelectItem extends InputItem {
    isMulti: boolean;
    checkBoxStyle: boolean;
    options: Array<Option>;

    constructor(code: string, label: string, prop: any = {}) {
        super(code, label, prop);
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

    setCheckBoxStyle(state: boolean): SelectItem {
        this.checkBoxStyle = state;
        return this;
    }

    setMulti(state: boolean): SelectItem {
        this.isMulti = state;
        return this;
    }
}

export enum TextType {
    TextArea, Default
}

export class TextItem extends InputItem {
    mode: TextType;

    constructor(code: string, label: string, prop: any = {}) {
        super(code, label, prop);
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
    constructor(code: string, label: string, prop: any = {}) {
        super(code, label, prop);
        this.comp = ElInput;
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string, prop: any = {}) {
        super(code, label, prop);
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

