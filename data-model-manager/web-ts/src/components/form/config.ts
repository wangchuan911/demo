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

import QueryInput from "@/components/form/input/QueryInput.vue"

export const InputModule = {
    QueryInput
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/
