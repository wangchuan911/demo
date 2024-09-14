import {ElInput} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';
import {FormContent} from "@/components/config";

export abstract class InputItem {
    code: string;
    label: string;
    comp: any;
    prop: any;
    propAsync: any;
    contentGetter: (() => FormContent) = () => null as unknown as FormContent;

    protected constructor(code: string, label: string, prop: any = {}) {
        this.code = code;
        this.label = label;
        this.prop = prop;
    }

    load(propAsync: Record<string, (prop: any, groups: FormContent) => void>): this {
        Object.keys(propAsync).forEach(key => {
            this.prop[key] = propAsync[key](this.prop, this.contentGetter());
        });
        return this;
    }

    setContent(getter: () => FormContent) {
        this.contentGetter = getter;
    }

}

export class MyOption {
    name: string;
    value: any;
    selected: boolean;

    constructor(name: string, value: any, selected: boolean) {
        this.name = name;
        this.value = value;
        this.selected = selected;
    }
}

export class SelectItem extends InputItem {
    isMulti: boolean;
    checkBoxStyle: boolean;

    constructor(code: string, label: string, prop: any = {}) {
        super(code, label, prop);
        this.isMulti = false;
        this.checkBoxStyle = false;
        this.comp = MySelect;
        this.prop['options'] = [];
    }

    setOptions(...options: Array<MyOption>): this {
        this.prop['options'].length = 0;
        this.addOptions(...options);
        return this;
    }

    addOptions(...options: Array<MyOption>): this {
        this.prop['options'].push(...options);
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

