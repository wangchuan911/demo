export interface FormItemConfig {
    type: string
    placeholder?: string
}

export class FormItemDefine {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    name!: string
    id!: string
    mode!: string
    init?: object
    config!: FormItemConfig
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/
