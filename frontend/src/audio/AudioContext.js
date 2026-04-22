import { createContext, useContext } from 'react'

export const AudioContextValue = createContext({
  isSoundEnabled: true,
  setIsSoundEnabled: () => {},
  playSound: () => {},
})

export function useAudio() {
  return useContext(AudioContextValue)
}
