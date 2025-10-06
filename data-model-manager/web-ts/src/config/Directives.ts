import {App, Directive} from 'vue'

abstract class DirectiveConfig {
    name: string

    constructor(name: string) {
        this.name = name;
    }

    abstract config(app: App): void
}

class TableScroll extends DirectiveConfig {
    getWarpByElement(target: Element): Element {
        return <Element>target.querySelector('.el-scrollbar__wrap')
    }

    _canLoadMore(selectwrap: Element): boolean {
        const sign = 0
        const scrollDistance =
            selectwrap.scrollHeight - selectwrap.scrollTop - selectwrap.clientHeight;
        return scrollDistance <= sign;
    }

    canLoadMore(target: Element): boolean {
        const warp = this.getWarpByElement(target);
        if (warp != null && this._canLoadMore(warp)) {
            return true
        }
        return false
    }

    config(app: App): void {
        const _this = this;
        app.directive(this.name, {
            mounted(el: Element, binding) {
                console.log(el)
                const selectwrap = _this.getWarpByElement(el);
                if (selectwrap == null) return
                selectwrap.$scrollFunction = function () {
                    // console.log(`this.scrollHeight:${this.scrollHeight} - this.scrollTop:${this.scrollTop} - this.clientHeight:${this.clientHeight}=${scrollDistance}`);
                    if (_this._canLoadMore(this)) {
                        binding.value()
                    }
                }
                selectwrap.addEventListener('scroll', selectwrap.$scrollFunction)
            },
            unmounted(el: Element) {
                const selectwrap = _this.getWarpByElement(el)
                if (selectwrap == null) return
                selectwrap.removeEventListener('scroll', selectwrap.$scrollFunction)
                selectwrap.$scrollFunction = null
            }
        } as Directive);
    }
}

const TableScrollConfig = new TableScroll('elTableScroll');

export default {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    config: (app: App) => {
        TableScrollConfig.config(app);
    },
    TableScrollConfig
};