import { describe, it, expect, beforeEach } from 'vitest';
import { useStore } from './store';

describe('useStore', () => {
  beforeEach(() => {
    // Reset store before each test if needed
    // useStore.getState().resetTask();
  });

  it('should have initial state', () => {
    const state = useStore.getState();
    expect(state.prompt).toBe('');
    expect(state.renderStatus).toBe('idle');
    expect(state.videoFormat).toBe('mp4');
    expect(state.videoQuality).toBe('1080p');
  });

  it('should update prompt', () => {
    useStore.getState().setPrompt('New prompt');
    expect(useStore.getState().prompt).toBe('New prompt');
  });

  it('should update render status', () => {
    useStore.getState().setRenderStatus('rendering');
    expect(useStore.getState().renderStatus).toBe('rendering');
  });

  it('should update video quality', () => {
    useStore.getState().setVideoQuality('720p');
    expect(useStore.getState().videoQuality).toBe('720p');
  });

  it('should reset task', () => {
    const state = useStore.getState();
    state.setGeneratedCode('test code');
    state.setRenderStatus('done');
    
    state.resetTask();
    
    const newState = useStore.getState();
    expect(newState.generatedCode).toBe('');
    expect(newState.renderStatus).toBe('idle');
  });
});
