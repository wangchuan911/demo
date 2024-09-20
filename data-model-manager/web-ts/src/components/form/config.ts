import {ElInput} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';
import {FormContent} from "@/components/config";

export declare type InputCompLoadedHandler<T> = (input: T, content: FormContent) => void;
export declare type ContentGetter = () => FormContent;


export abstract class InputItem {
    code: string;
    label: string;
    comp: any;
    prop: any;
    contentGetter: ContentGetter;
    inputLoadHandler: InputCompLoadedHandler<any>;

    protected constructor(code: string, label: string, inputLoadHandler: InputCompLoadedHandler<any> = (input, content) => {
        console.log("empty function");
    }) {
        this.code = code;
        this.label = label;
        this.prop = {};
        this.contentGetter = () => null as unknown as FormContent;
        this.inputLoadHandler = inputLoadHandler;
    }

    onLoaded(content: FormContent): this {
        this.setContent(() => content);
        this.inputLoadHandler(this, content);
        return this;
    }

    setContent(getter: ContentGetter) {
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

    constructor(code: string, label: string, prop: InputCompLoadedHandler<SelectItem> = (input, content) => {
        console.log("empty function");
    }) {
        super(code, label, prop);
        this.isMulti = false;
        this.checkBoxStyle = false;
        this.comp = MySelect;
        this.prop['options'] = [];
        this.prop['prop'] = {};
    }

    setOptions(...options: Array<MyOption>): this {
        if (this.prop['options'])
            this.prop['options'].length = 0;
        this.addOptions(...options);
        return this;
    }

    addOptions(...options: Array<MyOption>): this {
        if (this.prop['options'] == null)
            this.prop['options'] = [];
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

    constructor(code: string, label: string, prop: InputCompLoadedHandler<TextItem> = (input, content) => {
        console.log("empty function");
    }) {
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
    constructor(code: string, label: string, prop: InputCompLoadedHandler<QueryItem> = (input, content) => {
        console.log("empty function");
    }) {
        super(code, label, prop);
        this.comp = ElInput;
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string, prop: InputCompLoadedHandler<RadioItem> = (input, content) => {
        console.log("empty function");
    }) {
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

