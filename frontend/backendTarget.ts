type BackendEnvironment = Record<string, string | undefined>

export function resolveBackendTarget(
  fileEnvironment: BackendEnvironment,
  processEnvironment: BackendEnvironment,
) {
  const port = processEnvironment.BACKEND_PORT ?? fileEnvironment.BACKEND_PORT ?? '28080'
  return `http://localhost:${port}`
}
