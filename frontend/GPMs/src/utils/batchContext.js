const SELECTED_BATCH_KEY = 'gpms:selectedBatchId'

export const getSelectedBatchId = () => {
  const value = sessionStorage.getItem(SELECTED_BATCH_KEY)
  return value ? Number(value) : null
}

export const setSelectedBatchId = (batchId) => {
  if (batchId === null || batchId === undefined || batchId === '') {
    sessionStorage.removeItem(SELECTED_BATCH_KEY)
    return
  }
  sessionStorage.setItem(SELECTED_BATCH_KEY, String(batchId))
}

export const withSelectedBatchQuery = (path, batchId = getSelectedBatchId()) => {
  if (!batchId) return path
  return { path, query: { batchId } }
}
