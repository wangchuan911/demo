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

export abstract class InputItem {
    code: string;
    label: string;

    constructor(code: string, label: string) {

        this.code = code;
        this.label = label;
    }
}

export class selectItem extends InputItem {
    isMulti: boolean;
    checkBoxStyle: boolean;

    constructor(code: string, label: string) {
        super(code, label);
        this.isMulti = false;
        this.checkBoxStyle = false;
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
    }

    setMode(mode: TextType): TextItem {
        this.mode = mode;
        return this;
    }
}

export class QueryItem extends InputItem {
    constructor(code: string, label: string) {
        super(code, label);
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string) {
        super(code, label);
        this.isMulti = false;
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

