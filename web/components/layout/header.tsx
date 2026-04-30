import { Play, Settings, Sun, Moon, Flame, ChevronRight, Loader2 } from "lucide-react";
import { SiYoutube, SiTiktok, SiInstagram, SiX } from "react-icons/si";
import { Button } from "@/components/ui/button";
import {
  DropdownMenu,
  DropdownMenuContent,
  DropdownMenuItem,
  DropdownMenuLabel,
  DropdownMenuSeparator,
  DropdownMenuSub,
  DropdownMenuSubContent,
  DropdownMenuSubTrigger,
  DropdownMenuRadioGroup,
  DropdownMenuRadioItem,
  DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import { useTheme } from "@/app/providers";
import type { RenderSettings, Resolution, FrameRate, Format, AspectRatio } from "@/app/page";

const QUALITY_MATRIX: Record<Resolution, Record<FrameRate, string>> = {
  "480p":  { "24fps": "Low",  "30fps": "Low",  "60fps": "Low",  "120fps": "Low"  },
  "720p":  { "24fps": "Low",  "30fps": "Mid",  "60fps": "Mid",  "120fps": "Mid"  },
  "1080p": { "24fps": "Mid",  "30fps": "High", "60fps": "High", "120fps": "High" },
  "2k":    { "24fps": "High", "30fps": "High", "60fps": "QHD",  "120fps": "QHD"  },
  "4k":    { "24fps": "QHD",  "30fps": "QHD",  "60fps": "UHD",  "120fps": "UHD"  },
};

const QUALITY_COLORS: Record<string, string> = {
  Low:  "text-muted-foreground",
  Mid:  "text-blue-400",
  High: "text-green-500",
  QHD:  "text-primary",
  UHD:  "text-yellow-500",
};

const ASPECT_RATIOS: { value: AspectRatio; label: string; icons: React.ReactNode[] }[] = [
  {
    value: "16:9",
    label: "16 : 9",
    icons: [
      <SiYoutube   key="yt" className="h-3 w-3 text-red-500"        title="YouTube" />,
      <SiTiktok    key="tt" className="h-3 w-3 text-foreground/70"   title="TikTok" />,
      <SiInstagram key="ig" className="h-3 w-3 text-pink-500"       title="Instagram" />,
    ],
  },
  {
    value: "9:16",
    label: "9 : 16",
    icons: [
      <SiTiktok    key="tt" className="h-3 w-3 text-foreground/70"   title="TikTok" />,
      <SiInstagram key="ig" className="h-3 w-3 text-pink-500"       title="Instagram Reels" />,
      <SiYoutube   key="yt" className="h-3 w-3 text-red-500"        title="YouTube Shorts" />,
    ],
  },
  {
    value: "1:1",
    label: "1 : 1",
    icons: [
      <SiInstagram key="ig" className="h-3 w-3 text-pink-500"       title="Instagram Square" />,
      <SiX         key="x"  className="h-3 w-3 text-foreground/70"  title="X / Twitter" />,
    ],
  },
  { value: "4:3",  label: "4 : 3",  icons: [] },
  { value: "21:9", label: "21 : 9", icons: [] },
];

const QUALITY_ESTIMATES: Record<string, { seconds: number; range: string }> = {
  '480p':  { seconds: 20,  range: '10–40s' },
  '720p':  { seconds: 90,  range: '30–120s' },
  '1080p': { seconds: 180, range: '60–240s' },
  '2k':    { seconds: 300, range: '3–6 min' },
  '4k':    { seconds: 420, range: '4–8 min' },
};

interface HeaderProps {
  settings: RenderSettings;
  engineOnline: boolean;
  onRender?: () => void;
  isRendering?: boolean;
  elapsed?: number;
  renderTime?: number | null;
  generatedCode?: string;
}

export function Header({ settings, engineOnline, onRender, isRendering, elapsed = 0, renderTime = null, generatedCode = "" }: HeaderProps) {
  const { theme, toggleTheme } = useTheme();
  const { resolution, frameRate, format, aspectRatio,
          setResolution, setFrameRate, setFormat, setAspectRatio } = settings;

  const quality = QUALITY_MATRIX[resolution][frameRate];
  const qualityColor = QUALITY_COLORS[quality];

  return (
    <header className="flex h-14 items-center justify-between border-b border-border bg-background px-4 z-10 shrink-0">
      {/* Logo */}
      <div className="flex items-center gap-2.5">
        <div className="flex h-8 w-8 items-center justify-center rounded-xl bg-primary text-primary-foreground shadow-sm">
          <Flame className="h-4 w-4" />
        </div>
        <div className="flex flex-col leading-none">
          <span className="font-bold text-sm text-foreground tracking-tight">Manim Studio</span>
          <span className="text-[10px] text-muted-foreground">Animation Lab</span>
        </div>
      </div>

      {/* File pill */}
      <div className="hidden sm:flex items-center gap-2 text-xs text-muted-foreground font-mono bg-muted/60 rounded-lg px-3 py-1.5 border border-border/60">
        <div className={`h-1.5 w-1.5 rounded-full ${engineOnline ? "bg-orange-400 animate-pulse" : "bg-muted-foreground"}`} />
        <span>animation_scene.py</span>
        <span className="text-muted-foreground/40">— unsaved</span>
      </div>

      {/* Actions */}
      <div className="flex items-center gap-2">
        {/* Render Time Info */}
        <div className="flex flex-col items-end mr-2 text-right">
          <div className="text-[11px] text-muted-foreground font-mono">
            {renderTime !== null ? (
              `Rendered in ${renderTime}s`
            ) : isRendering ? (
              `Est. ${QUALITY_ESTIMATES[resolution]?.range || '30–120s'} · ${elapsed}s elapsed`
            ) : generatedCode ? (
              `~${QUALITY_ESTIMATES[resolution]?.range || '30–120s'} at ${resolution}`
            ) : null}
          </div>
          {(resolution === '1080p' || resolution === '2k' || resolution === '4k') && !isRendering && (
            <div className="text-[10px] text-orange-500 font-mono mt-0.5">
              {resolution === '4k' || resolution === '2k'
                ? 'Warning: High res may take 4–8 minutes'
                : 'High quality may take 2–4 minutes'}
            </div>
          )}
        </div>

        {/* Engine status */}
        <div className="hidden sm:flex items-center gap-1.5 px-2.5 py-1 rounded-lg bg-muted/50 border border-border/60 text-[10px] font-medium">
          <div className={`h-1.5 w-1.5 rounded-full ${engineOnline ? "bg-emerald-500 animate-pulse" : "bg-rose-500"}`} />
          <span className={engineOnline ? "text-emerald-500" : "text-rose-500"}>
            {engineOnline ? "Online" : "Offline"}
          </span>
        </div>

        <Button
          variant="ghost"
          size="icon"
          className="h-8 w-8 text-muted-foreground hover:text-foreground rounded-lg"
          onClick={toggleTheme}
          data-testid="button-theme-toggle"
          title={theme === "dark" ? "Switch to light mode" : "Switch to dark mode"}
        >
          {theme === "dark" ? <Sun className="h-4 w-4" /> : <Moon className="h-4 w-4" />}
        </Button>

        <Button
          size="sm"
          disabled={isRendering}
          onClick={onRender}
          className="h-8 gap-1.5 text-xs font-semibold rounded-lg bg-primary hover:bg-primary/90 text-primary-foreground shadow-sm glow-orange"
          data-testid="button-render"
        >
          {isRendering ? (
            <Loader2 className="h-3.5 w-3.5 animate-spin" />
          ) : (
            <Play className="h-3.5 w-3.5 fill-current" />
          )}
          {isRendering ? "Rendering..." : "Render"}
        </Button>

        <DropdownMenu>
          <DropdownMenuTrigger asChild>
            <Button
              variant="ghost"
              size="icon"
              className="h-8 w-8 text-muted-foreground hover:text-foreground rounded-lg"
              data-testid="button-settings"
            >
              <Settings className="h-4 w-4" />
            </Button>
          </DropdownMenuTrigger>

          <DropdownMenuContent align="end" className="w-64 rounded-xl border-border bg-popover shadow-lg">

            {/* Render Settings */}
            <DropdownMenuLabel className="text-[11px] text-muted-foreground font-semibold uppercase tracking-wider pt-3 pb-1 px-3">
              Render Settings
            </DropdownMenuLabel>
            <DropdownMenuSeparator />

            {/* Resolution */}
            <DropdownMenuSub>
              <DropdownMenuSubTrigger className="justify-between text-sm cursor-pointer rounded-lg my-0.5" data-testid="dropdown-resolution">
                <span>Resolution</span>
                <div className="flex items-center gap-1.5 ml-auto mr-1">
                  <span className="text-muted-foreground font-mono text-xs bg-muted px-2 py-0.5 rounded-md">{resolution}</span>
                  <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                </div>
              </DropdownMenuSubTrigger>
              <DropdownMenuSubContent className="rounded-xl border-border bg-popover shadow-lg w-36">
                <DropdownMenuRadioGroup value={resolution} onValueChange={v => setResolution(v as Resolution)}>
                  {(["480p", "720p", "1080p", "2k", "4k"] as Resolution[]).map(r => (
                    <DropdownMenuRadioItem key={r} value={r} className="font-mono text-sm cursor-pointer rounded-lg my-0.5">
                      {r}
                    </DropdownMenuRadioItem>
                  ))}
                </DropdownMenuRadioGroup>
              </DropdownMenuSubContent>
            </DropdownMenuSub>

            {/* Frame Rate */}
            <DropdownMenuSub>
              <DropdownMenuSubTrigger className="justify-between text-sm cursor-pointer rounded-lg my-0.5" data-testid="dropdown-framerate">
                <span>Frame Rate</span>
                <div className="flex items-center gap-1.5 ml-auto mr-1">
                  <span className="text-muted-foreground font-mono text-xs bg-muted px-2 py-0.5 rounded-md">{frameRate}</span>
                  <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                </div>
              </DropdownMenuSubTrigger>
              <DropdownMenuSubContent className="rounded-xl border-border bg-popover shadow-lg w-36">
                <DropdownMenuRadioGroup value={frameRate} onValueChange={v => setFrameRate(v as FrameRate)}>
                  {(["24fps", "30fps", "60fps", "120fps"] as FrameRate[]).map(f => (
                    <DropdownMenuRadioItem key={f} value={f} className="font-mono text-sm cursor-pointer rounded-lg my-0.5">
                      {f}
                    </DropdownMenuRadioItem>
                  ))}
                </DropdownMenuRadioGroup>
              </DropdownMenuSubContent>
            </DropdownMenuSub>

            {/* Quality — derived, read-only */}
            <DropdownMenuItem
              className="justify-between text-sm rounded-lg my-0.5 cursor-default select-none"
              data-testid="dropdown-quality"
              onSelect={e => e.preventDefault()}
            >
              <span>Quality</span>
              <span className={`text-xs font-semibold bg-muted px-2 py-0.5 rounded-md ${qualityColor}`}>
                {quality}
              </span>
            </DropdownMenuItem>

            <DropdownMenuSeparator />

            {/* Export Format */}
            <DropdownMenuLabel className="text-[11px] text-muted-foreground font-semibold uppercase tracking-wider pt-2 pb-1 px-3">
              Export Format
            </DropdownMenuLabel>
            <DropdownMenuSeparator />

            {/* Format */}
            <DropdownMenuSub>
              <DropdownMenuSubTrigger className="justify-between text-sm cursor-pointer rounded-lg my-0.5" data-testid="dropdown-format">
                <span>Format</span>
                <div className="flex items-center gap-1.5 ml-auto mr-1">
                  <span className="text-muted-foreground font-mono text-xs bg-muted px-2 py-0.5 rounded-md uppercase">{format}</span>
                  <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                </div>
              </DropdownMenuSubTrigger>
              <DropdownMenuSubContent className="rounded-xl border-border bg-popover shadow-lg w-36">
                <DropdownMenuRadioGroup value={format} onValueChange={v => setFormat(v as Format)}>
                  {(["mp4", "mkv", "gif", "webm"] as Format[]).map(f => (
                    <DropdownMenuRadioItem key={f} value={f} className="font-mono text-sm cursor-pointer rounded-lg my-0.5 uppercase">
                      {f}
                    </DropdownMenuRadioItem>
                  ))}
                </DropdownMenuRadioGroup>
              </DropdownMenuSubContent>
            </DropdownMenuSub>

            {/* Aspect Ratio */}
            <DropdownMenuSub>
              <DropdownMenuSubTrigger className="justify-between text-sm cursor-pointer rounded-lg my-0.5" data-testid="dropdown-aspect-ratio">
                <span>Aspect Ratio</span>
                <div className="flex items-center gap-1.5 ml-auto mr-1">
                  <span className="text-muted-foreground font-mono text-xs bg-muted px-2 py-0.5 rounded-md">{aspectRatio}</span>
                  <ChevronRight className="h-3.5 w-3.5 text-muted-foreground" />
                </div>
              </DropdownMenuSubTrigger>
              <DropdownMenuSubContent className="rounded-xl border-border bg-popover shadow-lg w-52">
                <DropdownMenuRadioGroup value={aspectRatio} onValueChange={v => setAspectRatio(v as AspectRatio)}>
                  {ASPECT_RATIOS.map(ar => (
                    <DropdownMenuRadioItem
                      key={ar.value}
                      value={ar.value}
                      className="text-sm cursor-pointer rounded-lg my-0.5"
                      data-testid={`aspect-ratio-${ar.value.replace(":", "-")}`}
                    >
                      <div className="flex items-center justify-between w-full">
                        <span className="font-mono">{ar.label}</span>
                        {ar.icons.length > 0 && (
                          <div className="flex items-center gap-1.5 ml-3">{ar.icons}</div>
                        )}
                      </div>
                    </DropdownMenuRadioItem>
                  ))}
                </DropdownMenuRadioGroup>
              </DropdownMenuSubContent>
            </DropdownMenuSub>

          </DropdownMenuContent>
        </DropdownMenu>
      </div>
    </header>
  );
}
