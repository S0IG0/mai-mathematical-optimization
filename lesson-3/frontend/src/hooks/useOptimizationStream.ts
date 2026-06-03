import { useCallback, useRef } from 'react';
import type { ContourData, Iteration, OptimizationParams, OptimizationResult, PathPoint, SurfaceData } from '../types';

export type StreamEvent =
  | { type: 'CONTOUR'; data: ContourData }
  | { type: 'SURFACE'; data: SurfaceData }
  | { type: 'STARTED'; data: { method: string } }
  | { type: 'PATH_POINT'; data: PathPoint }
  | { type: 'ITERATION'; data: Iteration }
  | { type: 'DONE'; data: OptimizationResult }
  | { type: 'ERROR'; data: { message: string } };

function wsUrl(): string {
  const proto = window.location.protocol === 'https:' ? 'wss:' : 'ws:';
  return `${proto}//${window.location.host}/ws/optimize`;
}

export interface StreamCallbacks {
  onContour?: (data: ContourData) => void;
  onSurface?: (data: SurfaceData) => void;
  onPathPoint?: (point: PathPoint, path: PathPoint[]) => void;
  onIteration?: (iteration: Iteration, iterations: Iteration[]) => void;
  onDone?: (result: OptimizationResult) => void;
  onError?: (message: string) => void;
}

export function useOptimizationStream() {
  const socketRef = useRef<WebSocket | null>(null);

  const cancel = useCallback(() => {
    if (socketRef.current) {
      socketRef.current.close();
      socketRef.current = null;
    }
  }, []);

  const run = useCallback(
    (params: OptimizationParams, callbacks: StreamCallbacks): Promise<OptimizationResult> => {
      cancel();

      return new Promise((resolve, reject) => {
        const path: PathPoint[] = [];
        const iterations: Iteration[] = [];
        const ws = new WebSocket(wsUrl());
        socketRef.current = ws;

        ws.onopen = () => {
          ws.send(JSON.stringify(params));
        };

        ws.onmessage = (event) => {
          try {
            const msg = JSON.parse(event.data as string) as StreamEvent;
            switch (msg.type) {
              case 'CONTOUR':
                callbacks.onContour?.(msg.data);
                break;
              case 'SURFACE':
                callbacks.onSurface?.(msg.data);
                break;
              case 'PATH_POINT':
                path.push(msg.data);
                callbacks.onPathPoint?.(msg.data, [...path]);
                break;
              case 'ITERATION':
                iterations.push(msg.data);
                callbacks.onIteration?.(msg.data, [...iterations]);
                break;
              case 'DONE':
                callbacks.onDone?.(msg.data);
                resolve(msg.data);
                ws.close();
                socketRef.current = null;
                break;
              case 'ERROR':
                callbacks.onError?.(msg.data.message);
                reject(new Error(msg.data.message));
                ws.close();
                socketRef.current = null;
                break;
              default:
                break;
            }
          } catch (e) {
            reject(e instanceof Error ? e : new Error('Ошибка разбора сообщения WebSocket'));
          }
        };

        ws.onerror = () => {
          reject(new Error('Ошибка WebSocket-соединения'));
        };

        ws.onclose = () => {
          socketRef.current = null;
        };
      });
    },
    [cancel],
  );

  return { run, cancel };
}
