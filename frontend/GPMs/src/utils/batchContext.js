const SELECTED_GRADE_KEY = 'gpms:selectedGrade'

export const getSelectedGrade = () => {
  return sessionStorage.getItem(SELECTED_GRADE_KEY) || null
}

export const setSelectedGrade = (grade) => {
  if (!grade) {
    sessionStorage.removeItem(SELECTED_GRADE_KEY)
    return
  }
  sessionStorage.setItem(SELECTED_GRADE_KEY, String(grade))
}

export const withSelectedGradeQuery = (path, grade = getSelectedGrade()) => {
  if (!grade) return path
  return { path, query: { grade } }
}

// keep old exports for backward compatibility during migration
export const getSelectedBatchId = () => {
  return getSelectedGrade()
}

export const setSelectedBatchId = (batchId) => {
  setSelectedGrade(batchId)
}

export const withSelectedBatchQuery = (path, batchId) => {
  return withSelectedGradeQuery(path, batchId)
}
