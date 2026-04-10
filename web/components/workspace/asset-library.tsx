import { useState } from "react";
import { ChevronUp, ChevronDown, Box, Type, Palette } from "lucide-react";
import { Button } from "@/components/ui/button";

interface AssetLibraryProps {
  isOpen: boolean;
  onToggle: () => void;
}

type Tab = "shapes" | "typography" | "palettes";

const TABS: { id: Tab; label: string; icon: React.ReactNode }[] = [
  { id: "shapes",     label: "Shapes",    icon: <Box  className="h-3.5 w-3.5" /> },
  { id: "typography", label: "Fonts",     icon: <Type className="h-3.5 w-3.5" /> },
  { id: "palettes",   label: "Palettes",  icon: <Palette className="h-3.5 w-3.5" /> },
];

/* ─── Shape assets ─────────────────────────────────────── */
const SHAPE_ASSETS: { name: string; icon: React.ReactNode }[] = [
  {
    name: "Sphere",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <circle cx="20" cy="20" r="14" stroke="currentColor" strokeWidth="1.5" />
        <ellipse cx="20" cy="20" rx="14" ry="6" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
        <line x1="20" y1="6" x2="20" y2="34" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
      </svg>
    ),
  },
  {
    name: "Cube",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <rect x="10" y="16" width="18" height="18" stroke="currentColor" strokeWidth="1.5" rx="1" />
        <path d="M10 16 L18 8 L36 8 L28 16" stroke="currentColor" strokeWidth="1.5" />
        <line x1="28" y1="8" x2="28" y2="26" stroke="currentColor" strokeWidth="1.5" />
        <line x1="28" y1="26" x2="28" y2="34" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
      </svg>
    ),
  },
  {
    name: "Torus",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <ellipse cx="20" cy="20" rx="14" ry="7" stroke="currentColor" strokeWidth="1.5" />
        <ellipse cx="20" cy="20" rx="6" ry="3" stroke="currentColor" strokeWidth="1" />
        <ellipse cx="20" cy="20" rx="14" ry="7" stroke="currentColor" strokeWidth="1.5" strokeDasharray="2 3" transform="rotate(20 20 20)" />
      </svg>
    ),
  },
  {
    name: "Cone",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <path d="M20 6 L34 34 L6 34 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
        <ellipse cx="20" cy="34" rx="14" ry="3" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
      </svg>
    ),
  },
  {
    name: "Cylinder",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <ellipse cx="20" cy="12" rx="12" ry="4" stroke="currentColor" strokeWidth="1.5" />
        <ellipse cx="20" cy="28" rx="12" ry="4" stroke="currentColor" strokeWidth="1.5" />
        <line x1="8" y1="12" x2="8" y2="28" stroke="currentColor" strokeWidth="1.5" />
        <line x1="32" y1="12" x2="32" y2="28" stroke="currentColor" strokeWidth="1.5" />
      </svg>
    ),
  },
  {
    name: "Plane",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <path d="M4 28 L20 12 L36 28 L20 36 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
        <line x1="4" y1="28" x2="36" y2="28" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
        <line x1="20" y1="12" x2="20" y2="36" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
      </svg>
    ),
  },
  {
    name: "Arrow",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <line x1="8" y1="20" x2="30" y2="20" stroke="currentColor" strokeWidth="2" strokeLinecap="round" />
        <path d="M24 13 L32 20 L24 27" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
      </svg>
    ),
  },
  {
    name: "Axis",
    icon: (
      <svg viewBox="0 0 40 40" className="w-full h-full" fill="none">
        <line x1="8" y1="32" x2="32" y2="32" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        <line x1="8" y1="32" x2="8" y2="8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" />
        <path d="M28 28 L33 32 L28 36" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
        <path d="M4 12 L8 7 L12 12" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round" />
        <line x1="8" y1="32" x2="20" y2="20" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2" />
        <path d="M17 17 L21 15 L19 19" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round" />
      </svg>
    ),
  },
];

/* ─── Font assets ──────────────────────────────────────── */
const FONT_ASSETS: { name: string; family: string; weight: string }[] = [
  { name: "Inter",        family: "Inter, sans-serif",           weight: "400" },
  { name: "Inter Bold",   family: "Inter, sans-serif",           weight: "700" },
  { name: "Mono",         family: "JetBrains Mono, monospace",   weight: "400" },
  { name: "Serif",        family: "Georgia, serif",              weight: "400" },
  { name: "Italic",       family: "Georgia, serif",              weight: "400" },
  { name: "Condensed",    family: "Arial Narrow, sans-serif",    weight: "700" },
];

/* ─── Palette assets ───────────────────────────────────── */
const PALETTE_ASSETS: { name: string; colors: string[] }[] = [
  { name: "Sunset",    colors: ["#f97316","#fb923c","#fbbf24","#fef08a"] },
  { name: "Ocean",     colors: ["#0ea5e9","#38bdf8","#7dd3fc","#e0f2fe"] },
  { name: "Forest",    colors: ["#16a34a","#4ade80","#bbf7d0","#f0fdf4"] },
  { name: "Neon",      colors: ["#a855f7","#ec4899","#f97316","#06b6d4"] },
  { name: "Monochrome",colors: ["#111827","#374151","#9ca3af","#f9fafb"] },
  { name: "Warm",      colors: ["#dc2626","#ea580c","#d97706","#ca8a04"] },
  { name: "Cool",      colors: ["#7c3aed","#2563eb","#0891b2","#059669"] },
  { name: "Pastel",    colors: ["#fda4af","#fdba74","#fde68a","#a5f3fc"] },
];

export function AssetLibrary({ isOpen, onToggle }: AssetLibraryProps) {
  const [activeTab, setActiveTab] = useState<Tab>("shapes");

  const totalCount =
    activeTab === "shapes" ? SHAPE_ASSETS.length :
    activeTab === "typography" ? FONT_ASSETS.length :
    PALETTE_ASSETS.length;

  return (
    <div
      className={`border-t border-border bg-background shrink-0 transition-all duration-300 ease-in-out flex flex-col ${
        isOpen ? "h-56" : "h-10"
      }`}
    >
      <div
        className="flex items-center justify-between px-4 h-10 shrink-0 cursor-pointer hover:bg-muted/30 transition-colors"
        onClick={onToggle}
        data-testid="button-asset-library-toggle"
      >
        <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
          <Box className="h-3.5 w-3.5 text-primary" />
          <span>Asset Library</span>
          {isOpen && (
            <span className="text-[10px] text-muted-foreground font-normal ml-1">
              {totalCount} assets
            </span>
          )}
        </div>
        <Button variant="ghost" size="icon" className="h-6 w-6 text-muted-foreground rounded-md">
          {isOpen ? <ChevronDown className="h-4 w-4" /> : <ChevronUp className="h-4 w-4" />}
        </Button>
      </div>

      {isOpen && (
        <div className="flex flex-1 overflow-hidden border-t border-border/50">
          {/* Tab navigation */}
          <div className="w-32 border-r border-border/50 p-1.5 flex flex-col gap-0.5 shrink-0 bg-sidebar">
            {TABS.map(tab => (
              <Button
                key={tab.id}
                variant="ghost"
                className={`justify-start h-8 text-xs font-medium rounded-xl gap-2 transition-colors ${
                  activeTab === tab.id
                    ? "bg-primary/15 text-primary hover:bg-primary/20"
                    : "text-muted-foreground hover:text-foreground hover:bg-accent"
                }`}
                onClick={() => setActiveTab(tab.id)}
                data-testid={`button-asset-tab-${tab.id}`}
              >
                {tab.icon}
                {tab.label}
              </Button>
            ))}
          </div>

          {/* Asset content */}
          <div className="flex-1 p-3 overflow-y-auto">

            {/* ── Shapes ── */}
            {activeTab === "shapes" && (
              <div className="grid grid-cols-4 sm:grid-cols-6 md:grid-cols-8 gap-2.5">
                {SHAPE_ASSETS.map((asset, i) => (
                  <div
                    key={i}
                    className="group relative aspect-square rounded-xl border border-border/60 bg-card overflow-hidden hover:border-primary/50 hover:bg-primary/5 transition-all cursor-pointer"
                    data-testid={`asset-shape-${i}`}
                  >
                    <div className="absolute inset-0 flex flex-col items-center justify-center p-2 gap-1">
                      <div className="w-3/4 h-3/4 text-muted-foreground group-hover:text-primary transition-colors">
                        {asset.icon}
                      </div>
                    </div>
                    <div className="absolute bottom-0 inset-x-0 py-0.5 bg-background/80 backdrop-blur-sm translate-y-full group-hover:translate-y-0 transition-transform duration-150 rounded-b-xl">
                      <span className="text-[9px] font-medium text-foreground block truncate text-center px-1">
                        {asset.name}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* ── Typography ── */}
            {activeTab === "typography" && (
              <div className="grid grid-cols-3 sm:grid-cols-4 md:grid-cols-6 gap-2.5">
                {FONT_ASSETS.map((font, i) => (
                  <div
                    key={i}
                    className="group relative aspect-square rounded-xl border border-border/60 bg-card overflow-hidden hover:border-primary/50 hover:bg-primary/5 transition-all cursor-pointer flex flex-col items-center justify-center gap-1"
                    data-testid={`asset-font-${i}`}
                  >
                    <span
                      className="text-2xl text-foreground/80 group-hover:text-primary transition-colors select-none leading-none"
                      style={{
                        fontFamily: font.family,
                        fontWeight: font.weight,
                        fontStyle: font.name === "Italic" ? "italic" : "normal",
                      }}
                    >
                      Aa
                    </span>
                    <div className="absolute bottom-0 inset-x-0 py-0.5 bg-background/80 backdrop-blur-sm translate-y-full group-hover:translate-y-0 transition-transform duration-150 rounded-b-xl">
                      <span className="text-[9px] font-medium text-foreground block truncate text-center px-1">
                        {font.name}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}

            {/* ── Palettes ── */}
            {activeTab === "palettes" && (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2.5">
                {PALETTE_ASSETS.map((palette, i) => (
                  <div
                    key={i}
                    className="group relative rounded-xl border border-border/60 bg-card overflow-hidden hover:border-primary/50 transition-all cursor-pointer"
                    data-testid={`asset-palette-${i}`}
                    style={{ aspectRatio: "2 / 1" }}
                  >
                    <div className="absolute inset-0 flex">
                      {palette.colors.map((color, ci) => (
                        <div
                          key={ci}
                          className="flex-1 h-full transition-all group-hover:opacity-90"
                          style={{ backgroundColor: color }}
                        />
                      ))}
                    </div>
                    <div className="absolute bottom-0 inset-x-0 py-0.5 bg-background/80 backdrop-blur-sm translate-y-full group-hover:translate-y-0 transition-transform duration-150 rounded-b-xl">
                      <span className="text-[9px] font-medium text-foreground block truncate text-center px-1">
                        {palette.name}
                      </span>
                    </div>
                  </div>
                ))}
              </div>
            )}

          </div>
        </div>
      )}
    </div>
  );
}
