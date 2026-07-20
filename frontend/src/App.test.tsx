import { render, screen } from '@testing-library/react'
import App from './App'

describe('Next Home shell', () => {
  it('presents the grade map as the main experience', () => {
    render(<App />)

    expect(screen.getByRole('heading', { name: '급지 지도로 시작하세요' })).toBeInTheDocument()
    expect(screen.getByLabelText('급지 지도 준비 영역')).toBeInTheDocument()
    expect(screen.getByText('현재 급지에서 1·2급지 위를 비교해보세요')).toBeInTheDocument()
  })
})
