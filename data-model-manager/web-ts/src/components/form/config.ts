export interface FormItemConfig {
    type: string
    placeholder: string
}

export class FormItemDefine {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    name: string | undefined
    id: string | undefined
    mode: string | undefined
    init: object | undefined
    config: FormItemConfig | undefined
}

/*export class FormDataItem {
    // eslint-disable-next-line @typescript-eslint/no-empty-function
    id: number | undefined
    value: string | undefined
    showValue: string | undefined
}*/
