import { useEffect, useMemo, useState } from 'react'

export type RegionSelection = {
  id: number
  code: string
  name: string
  provinceName: string
}

type RegionOption = { id: number; code: string; name: string }
type RegionGroup = RegionOption & { regions: RegionOption[] }

type RegionSelectorProps = {
  id: string
  label: string
  selectedRegionId?: number
  onSelect(region: RegionSelection): void
}

export default function RegionSelector({ id, label, selectedRegionId, onSelect }: RegionSelectorProps) {
  const [groups, setGroups] = useState<RegionGroup[]>([])
  const [provinceCode, setProvinceCode] = useState('')
  const [regionId, setRegionId] = useState('')
  const [error, setError] = useState('')

  useEffect(() => {
    const controller = new AbortController()
    fetch('/api/regions/options', { signal: controller.signal })
      .then((response) => {
        if (!response.ok) throw new Error('지역 목록을 불러오지 못했습니다.')
        return response.json() as Promise<RegionGroup[]>
      })
      .then(setGroups)
      .catch((reason: Error) => {
        if (reason.name !== 'AbortError') setError(reason.message)
      })
    return () => controller.abort()
  }, [])

  useEffect(() => {
    if (!selectedRegionId || groups.length === 0) return
    const parent = groups.find((group) => group.regions.some((region) => region.id === selectedRegionId))
    if (parent) {
      setProvinceCode(parent.code)
      setRegionId(String(selectedRegionId))
    }
  }, [groups, selectedRegionId])

  const selectedGroup = useMemo(
    () => groups.find((group) => group.code === provinceCode),
    [groups, provinceCode],
  )

  const chooseRegion = (value: string) => {
    setRegionId(value)
    const region = selectedGroup?.regions.find((item) => item.id === Number(value))
    if (region && selectedGroup) onSelect({ ...region, provinceName: selectedGroup.name })
  }

  return (
    <div className="region-selector" aria-label={label}>
      <label htmlFor={`${id}-province`}>
        <span>{label} 시·도</span>
        <select
          id={`${id}-province`}
          value={provinceCode}
          onChange={(event) => {
            setProvinceCode(event.target.value)
            setRegionId('')
          }}
        >
          <option value="">시·도 선택</option>
          {groups.map((group) => <option key={group.code} value={group.code}>{group.name}</option>)}
        </select>
      </label>
      <label htmlFor={`${id}-district`}>
        <span>{label} 시·군·구</span>
        <select
          id={`${id}-district`}
          value={regionId}
          onChange={(event) => chooseRegion(event.target.value)}
          disabled={!selectedGroup}
        >
          <option value="">시·군·구 선택</option>
          {selectedGroup?.regions.map((region) => (
            <option key={region.id} value={region.id}>{region.name}</option>
          ))}
        </select>
      </label>
      {error && <p role="alert" className="field-error">{error}</p>}
    </div>
  )
}
