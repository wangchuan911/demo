export interface FormItemConfig {
    type: string
    placeholder?: string

    [key: string]: any
}

export class FormItemDefine {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    name!: string
    id!: string
    mode!: string
    init?: any
    config!: FormItemConfig
}

import ObjectQueryInput from "@/components/form/input/ObjectQueryInput.vue"

export const InputModule = {
    ObjectQueryInput
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/
