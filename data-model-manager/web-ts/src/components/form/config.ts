import {ElInput} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';
import {FormContent} from "@/components/config";

export class Prop {
    prop: any;
    propAsync: Record<string, (input: InputItem, content: FormContent) => any>;

    constructor(prop: any = {}, propAsync: Record<string, (input: InputItem, content: FormContent) => any> = {}) {
        this.prop = prop;
        this.propAsync = propAsync;
    }
}

export abstract class InputItem {
    code: string;
    label: string;
    comp: any;
    prop: Prop;
    contentGetter: (() => FormContent) = () => null as unknown as FormContent;

    protected constructor(code: string, label: string, prop: Prop = {prop: {}, propAsync: {}} as Prop) {
        this.code = code;
        this.label = label;
        this.prop = prop;
    }

    onLoaded(content: FormContent): this {
        this.setContent(() => content);
        Object.keys(this.prop.propAsync).forEach(key => {
            const value: any = this.prop.propAsync[key](this, content);
            if (value instanceof Promise) {
                value.then(value1 => this.prop.prop[key] = value1);
            } else {
                this.prop.prop[key] = value;
            }
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

    constructor(code: string, label: string, prop: Prop) {
        super(code, label, prop);
        this.isMulti = false;
        this.checkBoxStyle = false;
        this.comp = MySelect;
        this.prop.prop['options'] = [];
        this.prop.prop['prop'] = {};
    }

    setOptions(...options: Array<MyOption>): this {
        if (this.prop.prop['options'])
            this.prop.prop['options'].length = 0;
        this.addOptions(...options);
        return this;
    }

    addOptions(...options: Array<MyOption>): this {
        if (this.prop.prop['options'] == null)
            this.prop.prop['options'] = [];
        this.prop.prop['options'].push(...options);
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

    constructor(code: string, label: string, prop: Prop) {
        super(code, label, prop);
        this.mode = TextType.Default;
        this.prop.prop['type'] = 'text';
        this.comp = ElInput;
    }

    setMode(mode: TextType): TextItem {
        this.mode = mode;
        return this;
    }
}

export class QueryItem extends InputItem {
    constructor(code: string, label: string, prop: Prop) {
        super(code, label, prop);
        this.comp = ElInput;
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string, prop: Prop) {
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

