import {ElInput, ElOption} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';
import MyEasySearch from '@/components/form/input/MyEasySearch.vue';
import {FormContent} from "@/components/config";

export declare type ContentGetter = () => FormContent;

export interface ItemConfig<T> {
    inputLoadHandler(input: T, content: FormContent): Promise<void>;

    inputChangeHandler(input: T, changeInput: InputItem, content: FormContent): Promise<void>;

    dataToValue(input: T, data: any, content: FormContent): Promise<void>;

    valueToData(input: T, form: Record<any, any>, content: FormContent): Promise<void>;

    /*constructor(inputLoadHandler: InputCompLoadedHandler<any> = (input, content) => {
        console.log("empty function");
    }, inputChangeHandler: InputCompChangeHandler = (input, content) => {
        console.log("empty function");
    }) {
        this.inputLoadHandler = inputLoadHandler;
        this.inputChangeHandler = inputChangeHandler;
    }*/
}

export abstract class InputItem implements ItemConfig<InputItem> {
    code: string;
    label: string;
    comp: any;
    prop: any;
    contentGetter: ContentGetter;
    events: any;
    value: any;
    config: ItemConfig<any>;

    protected constructor(code: string, label: string, config: ItemConfig<InputItem>) {
        this.code = code;
        this.label = label;
        this.prop = {};
        this.contentGetter = () => null as unknown as FormContent;
        this.config = config;
        const _this = this._self();
        this.events = {
            async change(value: any) {
                for (const input of _this.contentGetter().inputs) {
                    await input.inputChangeHandler(input, _this, _this.contentGetter());
                }
            }
        };
    }

    _self(): this {
        return this;
    }

    onLoaded(content: FormContent): this {
        this.setContent(() => content);
        this.inputLoadHandler(this, content);
        return this;
    }

    setContent(getter: ContentGetter) {
        this.contentGetter = getter;
    }

    check(): boolean {
        return true;
    }

    async dataToValue(input: any, data: any, content: FormContent): Promise<void> {
        if (typeof (this.config.dataToValue) == 'function') {
            return await this.config.dataToValue(input, data, content);
        }
        this.value = data[this.code];
    }

    async inputChangeHandler(input: any, changeInput: InputItem, content: FormContent): Promise<void> {
        if (typeof (this.config.inputChangeHandler) == 'function') {
            return await this.config.inputChangeHandler(input, changeInput, content);
        }
    }

    async inputLoadHandler(input: any, content: FormContent): Promise<void> {
        if (typeof (this.config.inputLoadHandler) == 'function') {
            return await this.config.inputLoadHandler(input, content);
        }
    }

    async valueToData(input: any, form: Record<any, any>, content: FormContent): Promise<void> {
        if (typeof (this.config.valueToData) == 'function') {
            return await this.config.valueToData(input, form, content);
        }
        form[this.code] = this.value;
    }
}

export class MyOption {
    name: string;
    value: any;
    selected: boolean;
    comp: any = ElOption;

    constructor(value: any, name: string, selected: boolean) {
        this.name = name;
        this.value = value;
        this.selected = selected;
    }
}

export class SelectItem extends InputItem {
    isMulti: boolean;
    checkBoxStyle: boolean;

    constructor(code: string, label: string, prop: ItemConfig<SelectItem> = {} as ItemConfig<SelectItem>) {
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

    constructor(code: string, label: string, prop: ItemConfig<TextItem> = {} as ItemConfig<TextItem>) {
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
    constructor(code: string, label: string, prop: ItemConfig<QueryItem> = {} as ItemConfig<QueryItem>) {
        super(code, label, prop);
        this.comp = ElInput;
    }
}

export class RadioItem extends InputItem {
    isMulti: boolean;

    constructor(code: string, label: string, prop: ItemConfig<RadioItem> = {} as ItemConfig<RadioItem>) {
        super(code, label, prop);
        this.isMulti = false;
        this.comp = ElInput;
    }

    setMulti(state: boolean): RadioItem {
        this.isMulti = state;
        return this;
    }
}

export class EasySearchItem extends InputItem {

    constructor(code: string, label: string, prop: ItemConfig<EasySearchItem> = {} as ItemConfig<EasySearchItem>) {
        super(code, label, prop);
        this.comp = MyEasySearch;
        this.prop['onSearch'] = () => {
            console.log("empty function");
        };
        this.prop['prop'] = {};
    }

    setOnSearch(fun: () => void): this {
        this.prop['onSearch'] = fun;
        return this;
    }
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/

