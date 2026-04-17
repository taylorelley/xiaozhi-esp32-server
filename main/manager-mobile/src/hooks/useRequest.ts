import type { Ref } from 'vue'

interface IUseRequestOptions<T> {
  /** Whether to */
  immediate?: boolean
  /** InitializeData */
  initialData?: T
}

interface IUseRequestReturn<T> {
  loading: Ref<boolean>
  error: Ref<boolean | Error>
  data: Ref<T | undefined>
  run: () => Promise<T | undefined>
}

/** * useRequestis of request，Used forProcessrequestandResponse。 * @param func request of Function，BackincludesResponseData of Promise。 * @param options includesrequestOption of Object {immediate, initialData}。 * @param options.immediate Whether torequest，Defaultisfalse。 * @param options.initialData InitializeData，Defaultisundefined。 * @returns BackObject{loading, error, data, run}，includesrequest of LoadStatus、ErrorInfo、ResponseDataandmanualrequest of Function。 */
export default function useRequest<T>(
  func: () => Promise<IResData<T>>,
  options: IUseRequestOptions<T> = { immediate: false },
): IUseRequestReturn<T> {
  const loading = ref(false)
  const error = ref(false)
  const data = ref<T | undefined>(options.initialData) as Ref<T | undefined>
  const run = async () => {
    loading.value = true
    return func()
      .then((res) => {
        data.value = res.data
        error.value = false
        return data.value
      })
      .catch((err) => {
        error.value = err
        throw err
      })
      .finally(() => {
        loading.value = false
      })
  }

  options.immediate && run()
  return { loading, error, data, run }
}
