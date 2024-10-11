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
    form: Record<any, any>;
    inputs: Array<InputItem>;

    constructor(form: Record<any, any>) {
        this.form = form;
        this.inputs = [];
    }

    addInput(...inputs: Array<InputItem>): this {
        this.inputs.push(...inputs);
        return this;
    }

    onLoaded() {
        this.inputs.forEach(value => {
            value.onLoaded(this);
        });
    }

    getForm(): Record<any, any> {
        let flag = false;
        for (const input of this.inputs) {
            flag = flag || !input.check();
        }
        if (flag) {
            throw "检查不通过";
        }
        return this.form;
    }
}

export function stringLike(search: string, ...arr: string[]) {
    return arr.find(value1 => search.includes(value1)) != null;
}