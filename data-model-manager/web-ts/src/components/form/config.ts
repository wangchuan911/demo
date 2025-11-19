import {ElInput, ElMessageBox, ElOption} from 'element-plus';
import MySelect from '@/components/form/input/MySelect.vue';
import MyTreeSelect from '@/components/form/input/MyTreeSelect.vue';
import MyEasySearch from '@/components/form/input/MyEasySearch.vue';
import {DrawersContent, FormContent} from "@/components/config";
import {reactive} from "vue";

export declare type ContentGetter = () => FormContent;


export interface ItemConfig<T> {
    inputLoadHandler(input: T, content: FormContent): Promise<void>;

    inputChangeHandler(input: T, changeInput: InputItem, value: any, content: FormContent): Promise<void>;

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
    config: ItemConfig<any>;

    protected constructor(code: string, label: string, config: ItemConfig<InputItem>) {
        this.code = code;
        this.label = label;
        this.prop = reactive({} as Record<any, any>);
        this.contentGetter = () => null as unknown as FormContent;
        this.config = config;
        const _this = this._self();
        this.events = {
            async change(value: any) {
                for (const input of _this.contentGetter().inputs) {
                    await input.inputChangeHandler(input, _this, value, _this.contentGetter());
                }
            }
        };
    }

    _self(): this {
        return this;
    }

    andThen(handler: (item: InputItem) => void): this {
        handler(this);
        return this;
    }

    async onLoaded(content: FormContent): Promise<this> {
        this.setContent(() => content);
        await this.inputLoadHandler(this, content);
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
        content.form[this.code] = data[this.code];
    }

    async inputChangeHandler(input: any, changeInput: InputItem, value: any, content: FormContent): Promise<void> {
        if (typeof (this.config.inputChangeHandler) == 'function') {
            return await this.config.inputChangeHandler(input, changeInput, value, content);
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
        form[this.code] = content.form[this.code];
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

export class FormDrawersContent extends DrawersContent {
    name: string;
    data: Record<any, any> | undefined;
    content: FormContent;
    event: any = {};

    constructor() {
        super();
        this.name = "提交数据";
        this.content = new FormContent({});
    }

    async open(data: Record<any, any>): Promise<DrawersContent> {
        this.data = data;
        this.content.reset(data);
        this._open();
        await this.content.onLoaded();
        return this;
    }

    beforeClose(done: () => void) {
        ElMessageBox.confirm('放弃保存?', {confirmButtonText: "确定", cancelButtonText: "取消"})
            .then(() => this.trigger('beforeCloseOk'))
            .then(() => {
                done();
            })
            .catch((e) => {
                // catch error
                this.trigger('beforeCloseFail', e);
            });
    }

    async trigger(name: string, ...args: Array<any>) {
        if (typeof (this.event[name]) == 'function') {
            await this.event[name](this, ...args);
        }
    }

    confirm() {
        console.log(this.content.getForm(true));
    }
}

export class MyTreeOption {
    value: any;
    label: string;
    children?: Array<MyTreeOption>;

    constructor(value: any, label: string, children?: Array<MyTreeOption>) {
        this.label = label;
        this.value = value;
        this.children = children;
    }
}

export class SelectTreeItem extends InputItem {

    constructor(code: string, label: string, prop: ItemConfig<SelectTreeItem> = {} as ItemConfig<SelectTreeItem>) {
        super(code, label, prop);
        this.comp = MyTreeSelect;
        this.prop['tree'] = [];
        this.prop['prop'] = {defaultExpandAll: false};
    }

    setOptions(...options: Array<MyTreeOption>): this {
        if (this.prop['tree'])
            this.prop['tree'].length = 0;
        this.addOptions(...options);
        return this;
    }

    addOptions(...options: Array<MyTreeOption>): this {
        if (this.prop['tree'] == null)
            this.prop['tree'] = [];
        this.prop['tree'].push(...options);
        return this;
    }


    setMulti(state: boolean): SelectTreeItem {
        this.prop['prop'].showCheckbox = state;
        return this;
    }
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/
export class KeyVal {
    key: string;
    val: string;

    constructor(key: string, val: string) {
        this.key = key;
        this.val = val;
    }
}

export class SearchFilterInputItem {
    value: any | null;
    displayValue: string | null;
    type: InputType;
    readonly: boolean;
    checked: boolean;
    enable: boolean;
    code: string;
    name: string;
    id: number;
    operator: KeyVal;

    constructor(id: number, code: string, name: string, type: InputType, operator: OperatorType, readonly1 = false, checked1 = false, enable1 = true) {
        this.type = type;
        this.readonly = readonly1;
        this.checked = checked1;
        this.enable = enable1;
        this.code = code;
        let typeDes;
        this.value = null;
        switch (type) {
            case InputType.text:
                typeDes = "文本";
                break;
            case InputType.decimal:
                typeDes = "小数";
                break;
            case InputType.int:
                typeDes = "整数";
                break;
            case InputType.time:
                typeDes = "时间";
                break;
            case InputType.boolean:
                typeDes = "布尔";
                this.value = true;
                break;
            default:
                typeDes = "未知";
                break;
        }
        this.name = `[${typeDes}]${name}`;
        this.id = id;
        this.operator = this.findOperator(operator);
        this.displayValue = null;
    }

    setValue(value: any | null = null, displayValue: string | null = null): this {
        this.value = value;
        if (value != null) {
            this.displayValue = displayValue || value;
        } else {
            this.displayValue = null;
        }
        return this;
    }

    setReadonly(value: boolean): this {
        this.readonly = value;
        return this;
    }

    setChecked(value: boolean): this {
        this.checked = value;
        return this;
    }

    setOperator(value: OperatorType): this {
        this.operator = this.findOperator(value);
        switch (value) {
            case OperatorType.range:
                this.value = [];
                break;
            case OperatorType.true:
            case OperatorType.false:
                this.value = (value == OperatorType.true);
                break;
            default:
                switch (this.type) {
                    case InputType.int:
                    case InputType.decimal:
                        this.value = 0;
                        break;
                    default:
                        this.value = "";
                        break;
                }
                break;
        }
        return this;
    }

    findOperator(value: OperatorType): KeyVal {
        return new KeyVal(value, FilterOperators.find(value1 => value1.id == value)?.name || ('未知' + value));
    }
}

export enum OperatorType {
    equal = "equal",
    startWith = "startWith",
    endWith = "endWith",
    contain = "contain",
    greatThan = "greatThan",
    lessThan = "lessThan",
    range = "range",
    true = "true",
    false = "false"

}

export enum InputType {
    text = "text",
    decimal = "decimal",
    int = "int",
    time = "time",
    boolean = "boolean"
}

export const FilterOperators = [
    {name: "等于", id: OperatorType.equal, type: [InputType.text, InputType.decimal, InputType.int]},
    {name: "起始于", id: OperatorType.startWith, type: [InputType.text]},
    {name: "结束于", id: OperatorType.endWith, type: [InputType.text]},
    {name: "包含", id: OperatorType.contain, type: [InputType.text]},
    {name: "大于", id: OperatorType.greatThan, type: [InputType.decimal, InputType.int, InputType.time]},
    {name: "小于", id: OperatorType.lessThan, type: [InputType.decimal, InputType.int, InputType.time]},
    {name: "范围", id: OperatorType.range, type: [InputType.decimal, InputType.int, InputType.time]},
    {name: "是", id: OperatorType.true, type: [InputType.boolean]},
    {name: "否", id: OperatorType.false, type: [InputType.boolean]}
];






