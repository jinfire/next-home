import { describe, expect, it } from 'vitest'
import { resolveBackendTarget } from './backendTarget'

describe('resolveBackendTarget', () => {
  it('builds the proxy target from the root env backend port', () => {
    expect(resolveBackendTarget({ BACKEND_PORT: '28080' }, {})).toBe('http://localhost:28080')
  })

  it('allows a process environment port to override the env file', () => {
    expect(resolveBackendTarget({ BACKEND_PORT: '28080' }, { BACKEND_PORT: '39090' })).toBe(
      'http://localhost:39090',
    )
  })
})
