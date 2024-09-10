export abstract class DrawersContent {

    show: boolean;

    abstract beforeClose(done: () => void): void;

    abstract confirm(): void;

    constructor() {
        this.show = false
    }

    _close(): void {
        this.show = false
    }

    _open(): void {
        this.show = true
    }
}