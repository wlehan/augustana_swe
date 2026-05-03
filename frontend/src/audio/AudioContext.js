import { createContext, useContext } from 'react'

/**
 * Audio context shared by pages and buttons that need to read or change sound
 * settings.
 */
export const AudioContextValue = createContext({
  isSoundEnabled: true,
  setIsSoundEnabled: () => {},
  playSound: () => {},
})

export function useAudio() {
  return useContext(AudioContextValue)
}
