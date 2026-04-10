import { useState, useRef, useEffect } from "react";
import { Play, Pause, SkipBack, SkipForward, Maximize, Download, MonitorPlay } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Slider } from "@/components/ui/slider";
import { Badge } from "@/components/ui/badge";
import type { Resolution, FrameRate, AspectRatio } from "@/app/page";

interface VideoOutputProps {
  resolution:  Resolution;
  frameRate:   FrameRate;
  aspectRatio: AspectRatio;
  videoUrl?: string | null;
  videoFilename?: string | null;
}

const ASPECT_RATIO_STYLE: Record<AspectRatio, React.CSSProperties> = {
  "16:9":  { aspectRatio: "16 / 9" },
  "9:16":  { aspectRatio: "9 / 16" },
  "1:1":   { aspectRatio: "1 / 1" },
  "4:3":   { aspectRatio: "4 / 3" },
  "21:9":  { aspectRatio: "21 / 9" },
};

const QUALITY_LABEL: Record<Resolution, Record<FrameRate, string>> = {
  "360p":  { "24fps": "Low",  "30fps": "Low",  "60fps": "Low",  "120fps": "Low"  },
  "720p":  { "24fps": "Low",  "30fps": "Mid",  "60fps": "Mid",  "120fps": "Mid"  },
  "1080p": { "24fps": "Mid",  "30fps": "High", "60fps": "High", "120fps": "High" },
  "2k":    { "24fps": "High", "30fps": "High", "60fps": "QHD",  "120fps": "QHD"  },
  "4k":    { "24fps": "QHD",  "30fps": "QHD",  "60fps": "UHD",  "120fps": "UHD"  },
};

function secondsToTimecode(s: number): string {
  const m = Math.floor(s / 60);
  const sec = Math.floor(s % 60);
  return `${m}:${sec.toString().padStart(2, "0")}`;
}

export function VideoOutput({ resolution, frameRate, aspectRatio, videoUrl, videoFilename }: VideoOutputProps) {
  const [playing, setPlaying] = useState(false);
  const [sliderValue, setSliderValue] = useState(0);
  const [duration, setDuration] = useState(0);
  const [currentTimeSec, setCurrentTimeSec] = useState(0);
  const videoRef = useRef<HTMLVideoElement>(null);

  const quality = QUALITY_LABEL[resolution][frameRate];

  useEffect(() => {
    if (videoUrl && videoRef.current) {
      videoRef.current.load();
      setSliderValue(0);
      setCurrentTimeSec(0);
    }
  }, [videoUrl]);

  const togglePlay = () => {
    if (!videoRef.current) {
      setPlaying(!playing);
      return;
    }
    if (playing) {
      videoRef.current.pause();
    } else {
      videoRef.current.play();
    }
    setPlaying(!playing);
  };

  const onTimeUpdate = () => {
    if (videoRef.current) {
      const current = videoRef.current.currentTime;
      const dur = videoRef.current.duration;
      setCurrentTimeSec(current);
      setSliderValue((current / dur) * 100);
    }
  };

  const onLoadedMetadata = () => {
    if (videoRef.current) {
      setDuration(videoRef.current.duration);
    }
  };

  const onSliderChange = (val: number) => {
    setSliderValue(val);
    if (videoRef.current && duration) {
      videoRef.current.currentTime = (val / 100) * duration;
    }
  };

  const handleDownload = () => {
    if (videoUrl) {
      const a = document.createElement('a');
      a.href = videoUrl;
      a.download = videoFilename || 'animation.mp4';
      a.click();
    }
  };

  const currentTimeDisplay = secondsToTimecode(currentTimeSec);
  const totalTimeDisplay = secondsToTimecode(duration || 60);

  return (
    <div className="flex flex-col h-full w-full relative">
      {/* Panel header */}
      <div className="flex items-center justify-between h-10 px-3 border-b border-border bg-card shrink-0 rounded-t-xl">
        <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
          <MonitorPlay className="h-3.5 w-3.5 text-primary" />
          <span>Preview</span>
        </div>
        <div className="flex items-center gap-1.5">
          <Badge className="h-5 px-2 text-[10px] font-mono bg-muted text-muted-foreground border-0 rounded-lg">
            {resolution}
          </Badge>
          <Badge className="h-5 px-2 text-[10px] font-mono bg-primary/15 text-primary border-0 rounded-lg font-semibold">
            {frameRate.replace("fps", " FPS")}
          </Badge>
          <Badge className="h-5 px-2 text-[10px] font-mono bg-muted/60 text-muted-foreground border-0 rounded-lg">
            {quality}
          </Badge>
        </div>
      </div>

      {/* Viewport */}
      <div className="flex-1 bg-[#080808] relative flex items-center justify-center overflow-hidden p-4">
        <div
          className="absolute inset-0 opacity-20"
          style={{
            backgroundImage:
              "linear-gradient(rgba(249,115,22,0.07) 1px, transparent 1px), linear-gradient(90deg, rgba(249,115,22,0.07) 1px, transparent 1px)",
            backgroundSize: "40px 40px",
          }}
        />

        {/* Aspect-ratio canvas */}
        <div
          className="relative bg-[#0a0a0a] rounded-lg overflow-hidden border border-orange-500/10 shadow-xl flex items-center justify-center"
          style={{
            ...ASPECT_RATIO_STYLE[aspectRatio],
            maxWidth: "100%",
            maxHeight: "100%",
            width: aspectRatio === "9:16" ? "auto" : "100%",
          }}
        >
          {videoUrl ? (
            <video
              ref={videoRef}
              src={videoUrl}
              className="w-full h-full object-contain"
              onTimeUpdate={onTimeUpdate}
              onLoadedMetadata={onLoadedMetadata}
              onEnded={() => setPlaying(false)}
            />
          ) : (
            <div className="relative flex items-center justify-center w-36 h-36">
              <div className="absolute w-36 h-36 rounded-full border border-orange-500/30" style={{ animation: "spin 18s linear infinite" }} />
              <div className="absolute w-24 h-24 rounded-full border border-orange-400/20" style={{ transform: "scaleY(0.5)", animation: "spin 12s linear infinite reverse" }} />
              <div className="absolute w-16 h-16 rounded-full border border-orange-300/20" style={{ transform: "scaleY(0.5) rotate(45deg)", animation: "spin 8s linear infinite" }} />
              <div className="absolute w-14 h-14 bg-orange-500/15 blur-2xl rounded-full" />
              <div className="absolute w-6 h-6 bg-orange-400/60 blur-md rounded-full" />
              <div className="absolute w-2.5 h-2.5 bg-orange-400 rounded-full" style={{ boxShadow: "0 0 12px 4px rgba(249,115,22,0.6)" }} />
            </div>
          )}

          {/* Timecode overlay */}
          <div className="absolute top-2.5 right-2.5 font-mono text-[10px] text-white/50 bg-black/60 px-2 py-0.5 rounded-md backdrop-blur-sm border border-white/10">
            {currentTimeDisplay.padStart(4, "0")}
          </div>

          {/* Aspect ratio label */}
          <div className="absolute bottom-2.5 right-2.5 font-mono text-[10px] text-white/30 bg-black/40 px-2 py-0.5 rounded-md">
            {aspectRatio}
          </div>

          {/* Preview pill */}
          {!videoUrl && (
            <div className="absolute top-2.5 left-2.5 flex items-center gap-1.5 text-[10px] text-orange-400/80 bg-black/60 px-2 py-0.5 rounded-md backdrop-blur-sm border border-orange-500/20">
              <div className="h-1.5 w-1.5 rounded-full bg-orange-400 animate-pulse" />
              Preview
            </div>
          )}
        </div>
      </div>

      {/* Playback controls */}
      <div className="border-t border-border bg-card shrink-0 px-3 py-2.5 flex flex-col gap-2">
        <div className="flex items-center gap-2">
          <span className="text-[10px] font-mono text-muted-foreground w-8 text-right tabular-nums">
            {currentTimeDisplay}
          </span>
          <Slider
            value={[sliderValue]}
            onValueChange={([v]) => onSliderChange(v)}
            max={100}
            step={0.1}
            className="flex-1"
            data-testid="slider-timeline"
          />
          <span className="text-[10px] font-mono text-muted-foreground w-8 tabular-nums">
            {totalTimeDisplay}
          </span>
        </div>

        <div className="flex items-center justify-between">
          <div className="flex items-center gap-1">
            <Button
              variant="ghost" size="icon"
              className="h-7 w-7 rounded-lg text-muted-foreground hover:text-foreground"
              onClick={() => onSliderChange(0)}
              data-testid="button-skip-back"
            >
              <SkipBack className="h-3.5 w-3.5" />
            </Button>
            <Button
              size="icon"
              className="h-8 w-8 rounded-xl bg-primary hover:bg-primary/90 text-primary-foreground shadow-sm"
              onClick={togglePlay}
              data-testid="button-play-pause"
            >
              {playing ? <Pause className="h-3.5 w-3.5 fill-current" /> : <Play className="h-3.5 w-3.5 fill-current" />}
            </Button>
            <Button
              variant="ghost" size="icon"
              className="h-7 w-7 rounded-lg text-muted-foreground hover:text-foreground"
              onClick={() => onSliderChange(100)}
              data-testid="button-skip-forward"
            >
              <SkipForward className="h-3.5 w-3.5" />
            </Button>
          </div>

          <div className="flex items-center gap-1">
            <Button variant="ghost" size="icon" className="h-7 w-7 rounded-lg text-muted-foreground hover:text-foreground" data-testid="button-fullscreen">
              <Maximize className="h-3.5 w-3.5" />
            </Button>
            <Button 
              variant="outline" 
              size="sm" 
              className="h-7 gap-1.5 text-[11px] font-semibold rounded-lg" 
              data-testid="button-export"
              onClick={handleDownload}
              disabled={!videoUrl}
            >
              <Download className="h-3 w-3" />
              Export
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
