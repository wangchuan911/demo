import {InputItem} from "@/components/form/config";

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

    inputs: Array<InputItem> = [];


    addInput(...inputs: Array<InputItem>): this {
        this.inputs.push(...inputs);
        return this;
    }

    onLoaded() {
        this.inputs.forEach(value => {
            value.onLoaded(this);
        });
    }

    async getForm(check: boolean): Promise<Record<any, any>> {
        let flag = false;
        const form: Record<any, any> = {};
        for (const input of this.inputs) {
            await input.valueToData(input, form, this);
        }
        if (check) {
            for (const input of this.inputs) {
                flag = flag || !input.check();
            }
            if (flag) {
                throw "检查不通过";
            }
        }
        return form;
    }
}

export function stringLike(search: string, ...arr: string[]) {
    return arr.find(value1 => search.includes(value1)) != null;
}