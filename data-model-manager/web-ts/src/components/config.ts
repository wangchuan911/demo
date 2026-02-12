import {InputItem} from "@/components/form/config";
import {reactive, ref} from "vue";
import {FormInstance, FormRules} from "element-plus";
import {Reactive, Ref} from "@vue/reactivity";

export abstract class DrawersContent {

    show: boolean;

    abstract beforeClose(done: () => void): void;

    abstract confirm(): void;

    protected constructor() {
        this.show = false;
    }

    _close(): void {
        this.show = false;
    }

    _open(): void {
        this.show = true;
    }
}

export class FormContent {
    form: Record<any, any>;
    inputs: Array<InputItem>;
    formRef: Ref<FormInstance | undefined> | undefined

    constructor(form: Record<any, any> = {}) {
        this.form = form;
        this.inputs = [];
        this.formRef = undefined;
    }

    addInput(...inputs: Array<InputItem>): this {
        this.inputs.push(...inputs);
        return this;
    }



    setInputState(code: string, visible: boolean): this {
        for (let input of this.inputs) {
            if (input.code == code) {
                input.disable = !visible;
            }
        }
        return this;
    }

    async onLoaded(): Promise<void> {
        for (const input of this.inputs) {
            if (input.disable) continue;
            await input.onLoaded(this);
        }
    }

    async getForm(): Promise<Record<any, any>> {
        let flag = false;
        const form: Record<any, any> = {};
        for (const input of this.inputs) {
            if (input.disable) continue;
            await input.valueToData(input, form, this);
        }
        console.log("原始", this.form);
        console.log("转换", form);
        console.log("转换(string)", JSON.stringify(form));
        return form;
    }

    reset(data: any = {}): void {
        this.form = {};
        for (const input of this.inputs) {
            if (input.disable) continue;
            input.dataToValue(input, data, this);
        }
    }
}

export function stringLike(search: string, ...arr: string[]) {
    return arr.find(value1 => search.includes(value1)) != null;
}