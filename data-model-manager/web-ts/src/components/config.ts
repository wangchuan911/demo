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
        inputs.forEach(value => {
            value.setGroup(() => this.inputs);
        });
        return this;
    }
}

export function stringLike(search: string, ...arr: string[]) {
    return arr.find(value1 => search.includes(value1)) != null;
}