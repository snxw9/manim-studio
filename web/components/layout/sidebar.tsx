import { useState } from "react";
import {
  ChevronRight, ChevronDown, ChevronUp,
  Shapes, FunctionSquare, Infinity, LayoutGrid, Sparkles, Star,
  Box, Type, Palette, BarChart2,
} from "lucide-react";
import { ScrollArea } from "@/components/ui/scroll-area";
import { Button } from "@/components/ui/button";
import { TEMPLATES } from "@/lib/templates";

/* ─── Asset library data ─────────────────────────────────── */

type AssetTab = "shapes" | "plots" | "typography" | "palettes";

const ASSET_TABS: { id: AssetTab; label: string; icon: React.ReactNode }[] = [
  { id: "shapes",     label: "Shapes", icon: <Box       className="h-3 w-3" /> },
  { id: "plots",      label: "Plots",  icon: <BarChart2 className="h-3 w-3" /> },
  { id: "typography", label: "Fonts",  icon: <Type      className="h-3 w-3" /> },
  { id: "palettes",   label: "Colors", icon: <Palette   className="h-3 w-3" /> },
];

const SHAPE_ASSETS: { name: string; icon: React.ReactNode }[] = [
  { name: "Circle",   icon: <svg viewBox="0 0 40 40" fill="none"><circle cx="20" cy="20" r="14" stroke="currentColor" strokeWidth="1.5"/><ellipse cx="20" cy="20" rx="14" ry="6" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2"/><line x1="20" y1="6" x2="20" y2="34" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2"/></svg> },
  { name: "Square",   icon: <svg viewBox="0 0 40 40" fill="none"><rect x="10" y="16" width="18" height="18" stroke="currentColor" strokeWidth="1.5" rx="1"/><path d="M10 16 L18 8 L36 8 L28 16" stroke="currentColor" strokeWidth="1.5"/><line x1="28" y1="8" x2="28" y2="26" stroke="currentColor" strokeWidth="1.5"/></svg> },
  { name: "Torus",    icon: <svg viewBox="0 0 40 40" fill="none"><ellipse cx="20" cy="20" rx="14" ry="7" stroke="currentColor" strokeWidth="1.5"/><ellipse cx="20" cy="20" rx="6" ry="3" stroke="currentColor" strokeWidth="1"/></svg> },
  { name: "Cone",     icon: <svg viewBox="0 0 40 40" fill="none"><path d="M20 6 L34 34 L6 34 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/><ellipse cx="20" cy="34" rx="14" ry="3" stroke="currentColor" strokeWidth="1" strokeDasharray="2 2"/></svg> },
  { name: "Cylinder", icon: <svg viewBox="0 0 40 40" fill="none"><ellipse cx="20" cy="12" rx="12" ry="4" stroke="currentColor" strokeWidth="1.5"/><ellipse cx="20" cy="28" rx="12" ry="4" stroke="currentColor" strokeWidth="1.5"/><line x1="8" y1="12" x2="8" y2="28" stroke="currentColor" strokeWidth="1.5"/><line x1="32" y1="12" x2="32" y2="28" stroke="currentColor" strokeWidth="1.5"/></svg> },
  { name: "Plane",    icon: <svg viewBox="0 0 40 40" fill="none"><path d="M4 28 L20 12 L36 28 L20 36 Z" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/></svg> },
  { name: "Arrow",    icon: <svg viewBox="0 0 40 40" fill="none"><line x1="8" y1="20" x2="30" y2="20" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/><path d="M24 13 L32 20 L24 27" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/></svg> },
  { name: "Axes",     icon: <svg viewBox="0 0 40 40" fill="none"><line x1="8" y1="32" x2="32" y2="32" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/><line x1="8" y1="32" x2="8" y2="8" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/><path d="M28 28 L33 32 L28 36" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/><path d="M4 12 L8 7 L12 12" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/></svg> },
];

const PLOT_ASSETS: { name: string; icon: React.ReactNode }[] = [
  {
    name: "NumberLine",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="4" y1="20" x2="36" y2="20" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <path d="M33 17 L37 20 L33 23" stroke="currentColor" strokeWidth="1.5" strokeLinejoin="round"/>
      {[10,16,20,24,30].map(x => <line key={x} x1={x} y1="17" x2={x} y2="23" stroke="currentColor" strokeWidth="1.2"/>)}
    </svg>,
  },
  {
    name: "Axes",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="6" y1="34" x2="36" y2="34" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <line x1="6" y1="34" x2="6" y2="6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round"/>
      <path d="M33 31 L37 34  33 37" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
      <path d="M3 9 L6 5 L9 9" stroke="currentColor" strokeWidth="1.2" strokeLinejoin="round"/>
      <path d="M6 28 Q14 14 28 18" stroke="currentColor" strokeWidth="1.2" fill="none" strokeLinecap="round"/>
    </svg>,
  },
  {
    name: "BarChart",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="5" y1="35" x2="35" y2="35" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="8"  y="22" width="5" height="13" stroke="currentColor" strokeWidth="1.2" rx="1"/>
      <rect x="16" y="14" width="5" height="21" stroke="currentColor" strokeWidth="1.2" rx="1"/>
      <rect x="24" y="18" width="5" height="17" stroke="currentColor" strokeWidth="1.2" rx="1"/>
    </svg>,
  },
  {
    name: "PieChart",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <circle cx="20" cy="20" r="14" stroke="currentColor" strokeWidth="1.2"/>
      <line x1="20" y1="20" x2="20" y2="6" stroke="currentColor" strokeWidth="1.2"/>
      <line x1="20" y1="20" x2="32" y2="28" stroke="currentColor" strokeWidth="1.2"/>
      <line x1="20" y1="20" x2="6" y2="26" stroke="currentColor" strokeWidth="1.2"/>
    </svg>,
  },
  {
    name: "FunctionGraph",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="4" y1="36" x2="36" y2="36" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
      <line x1="4" y1="36" x2="4" y2="6" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
      <path d="M4 30 C10 30 10 10 20 10 C30 10 30 30 36 28" stroke="currentColor" strokeWidth="1.5" fill="none" strokeLinecap="round"/>
    </svg>,
  },
  {
    name: "ScatterPlot",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="4" y1="36" x2="36" y2="36" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
      <line x1="4" y1="36" x2="4" y2="6" stroke="currentColor" strokeWidth="1.2" strokeLinecap="round"/>
      {[[10,28],[15,20],[18,24],[22,14],[27,18],[30,12],[33,16]].map(([x,y],i) =>
        <circle key={i} cx={x} cy={y} r="2" stroke="currentColor" strokeWidth="1.2"/>
      )}
    </svg>,
  },
  {
    name: "Histogram",
    icon: <svg viewBox="0 0 40 40" fill="none">
      <line x1="4" y1="35" x2="36" y2="35" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="5"  y="28" width="5" height="7"  stroke="currentColor" strokeWidth="1.2"/>
      <rect x="10" y="20" width="5" height="15" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="15" y="14" width="5" height="21" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="20" y="18" width="5" height="17" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="25" y="24" width="5" height="11" stroke="currentColor" strokeWidth="1.2"/>
      <rect x="30" y="30" width="5" height="5"  stroke="currentColor" strokeWidth="1.2"/>
    </svg>,
  },
  {
    name: "NumberPlane",
    icon: <svg viewBox="0 0 40 40" fill="none">
      {[10,20,30].map(x => <line key={`v${x}`} x1={x} y1="4" x2={x} y2="36" stroke="currentColor" strokeWidth="0.8" strokeDasharray="2 2"/>)}
      {[10,20,30].map(y => <line key={`h${y}`} x1="4" y1={y} x2="36" y2={y} stroke="currentColor" strokeWidth="0.8" strokeDasharray="2 2"/>)}
      <line x1="4" y1="20" x2="36" y2="20" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
      <line x1="20" y1="4" x2="20" y2="36" stroke="currentColor" strokeWidth="1.4" strokeLinecap="round"/>
    </svg>,
  },
];

const FONT_ASSETS: { name: string; family: string; weight: string; italic?: boolean }[] = [
  { name: "Inter",     family: "Inter, sans-serif",         weight: "400" },
  { name: "Bold",      family: "Inter, sans-serif",         weight: "700" },
  { name: "Mono",      family: "JetBrains Mono, monospace", weight: "400" },
  { name: "Serif",     family: "Georgia, serif",            weight: "400" },
  { name: "Italic",    family: "Georgia, serif",            weight: "400", italic: true },
  { name: "Condensed", family: "Arial Narrow, sans-serif",  weight: "700" },
];

const PALETTE_ASSETS: { name: string; colors: string[] }[] = [
  { name: "Sunset",  colors: ["#f97316","#fb923c","#fbbf24","#fef08a"] },
  { name: "Ocean",   colors: ["#0ea5e9","#38bdf8","#7dd3fc","#e0f2fe"] },
  { name: "Forest",  colors: ["#16a34a","#4ade80","#bbf7d0","#f0fdf4"] },
  { name: "Neon",    colors: ["#a855f7","#ec4899","#f97316","#06b6d4"] },
  { name: "Mono",    colors: ["#111827","#374151","#9ca3af","#f9fafb"] },
  { name: "Warm",    colors: ["#dc2626","#ea580c","#d97706","#ca8a04"] },
];

/* ─── Sidebar ────────────────────────────────────────────── */

interface SidebarProps {
  onTemplateClick?: (templateId: string) => void;
  selectedTemplate?: string | null;
}

export function Sidebar({ onTemplateClick, selectedTemplate }: SidebarProps) {
  const [assetOpen,   setAssetOpen]   = useState(false);
  const [activeAsset, setActiveAsset] = useState<AssetTab>("shapes");

  const categories = Array.from(new Set(TEMPLATES.map(t => t.category)));

  return (
    <div className="w-60 border-r border-border bg-sidebar flex flex-col shrink-0 h-full overflow-hidden">

      {/* Templates header */}
      <div className="px-4 py-3 border-b border-border shrink-0">
        <p className="text-xs font-semibold text-muted-foreground uppercase tracking-wider">Templates</p>
        <p className="text-[11px] text-muted-foreground/60 mt-0.5">Click to load into editor</p>
      </div>

      {/* Templates list */}
      <ScrollArea className="flex-1 h-0">
        <div className="p-0">
          {categories.map(category => (
            <div key={category} className="mb-2">
              <div className="text-[9px] text-muted-foreground/50 font-bold uppercase tracking-widest px-4 pt-4 pb-1">
                {category}
              </div>
              <div className="space-y-0.5 px-2">
                {TEMPLATES.filter(t => t.category === category).map(template => (
                  <button
                    key={template.id}
                    onClick={() => onTemplateClick?.(template.id)}
                    className={`w-full group flex flex-col items-flex-start py-2 px-3 rounded-xl transition-all text-left ${
                      selectedTemplate === template.id
                        ? "bg-primary/10 border-primary/20"
                        : "hover:bg-accent border-transparent"
                    } border`}
                    data-testid={`template-${template.id}`}
                  >
                    <div className="flex items-center gap-2 mb-0.5">
                      <Star className={`h-2.5 w-2.5 ${selectedTemplate === template.id ? "text-primary fill-primary" : "text-muted-foreground/30 group-hover:text-primary"} transition-colors`} />
                      <span className={`text-[12px] font-semibold ${selectedTemplate === template.id ? "text-primary" : "text-foreground"} transition-colors`}>
                        {template.name}
                      </span>
                    </div>
                    <span className="text-[10px] text-muted-foreground/60 line-clamp-1 pl-4">
                      {template.description}
                    </span>
                  </button>
                ))}
              </div>
            </div>
          ))}
        </div>
      </ScrollArea>

      {/* ── Asset Library ───────────────────────────────────── */}
      <div className={`border-t border-border shrink-0 flex flex-col transition-all duration-300 ease-in-out ${assetOpen ? "h-64" : "h-10"}`}>

        {/* Toggle header */}
        <div
          className="flex items-center justify-between px-3 h-10 cursor-pointer hover:bg-muted/30 transition-colors shrink-0"
          onClick={() => setAssetOpen(p => !p)}
          data-testid="button-asset-library-toggle"
        >
          <div className="flex items-center gap-2 text-xs font-semibold text-foreground/80">
            <Box className="h-3.5 w-3.5 text-primary" />
            <span>Asset Library</span>
          </div>
          <Button variant="ghost" size="icon" className="h-5 w-5 text-muted-foreground rounded-md">
            {assetOpen ? <ChevronDown className="h-3.5 w-3.5" /> : <ChevronUp className="h-3.5 w-3.5" />}
          </Button>
        </div>

        {/* Asset content */}
        {assetOpen && (
          <div className="flex flex-col flex-1 overflow-hidden border-t border-border/50">

            {/* Tab strip */}
            <div className="flex gap-0.5 px-2 pt-1.5 shrink-0">
              {ASSET_TABS.map(tab => (
                <button
                  key={tab.id}
                  onClick={() => setActiveAsset(tab.id)}
                  data-testid={`button-asset-tab-${tab.id}`}
                  className={`flex items-center gap-1 px-2 py-1 text-[10px] font-semibold rounded-lg transition-colors ${
                    activeAsset === tab.id
                      ? "bg-primary/15 text-primary"
                      : "text-muted-foreground hover:text-foreground hover:bg-muted/40"
                  }`}
                >
                  {tab.icon}
                  {tab.label}
                </button>
              ))}
            </div>

            {/* Grid */}
            <div className="flex-1 overflow-y-auto p-2">

              {activeAsset === "shapes" && (
                <div className="grid grid-cols-4 gap-1.5">
                  {SHAPE_ASSETS.map((asset, i) => (
                    <div
                      key={i}
                      className="group relative aspect-square rounded-lg border border-border/60 bg-card hover:border-primary/50 hover:bg-primary/5 transition-all cursor-pointer overflow-hidden"
                      data-testid={`asset-shape-${i}`}
                      title={asset.name}
                    >
                      <div className="absolute inset-0 flex items-center justify-center p-2 text-muted-foreground group-hover:text-primary transition-colors">
                        {asset.icon}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {activeAsset === "plots" && (
                <div className="grid grid-cols-4 gap-1.5">
                  {PLOT_ASSETS.map((asset, i) => (
                    <div
                      key={i}
                      className="group relative aspect-square rounded-lg border border-border/60 bg-card hover:border-primary/50 hover:bg-primary/5 transition-all cursor-pointer overflow-hidden"
                      data-testid={`asset-plot-${i}`}
                      title={asset.name}
                    >
                      <div className="absolute inset-0 flex items-center justify-center p-2 text-muted-foreground group-hover:text-primary transition-colors">
                        {asset.icon}
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {activeAsset === "typography" && (
                <div className="grid grid-cols-3 gap-1.5">
                  {FONT_ASSETS.map((font, i) => (
                    <div
                      key={i}
                      className="group aspect-square rounded-lg border border-border/60 bg-card hover:border-primary/50 hover:bg-primary/5 transition-all cursor-pointer flex flex-col items-center justify-center"
                      data-testid={`asset-font-${i}`}
                      title={font.name}
                    >
                      <span
                        className="text-xl text-foreground/80 group-hover:text-primary transition-colors select-none leading-none"
                        style={{
                          fontFamily: font.family,
                          fontWeight: font.weight,
                          fontStyle: font.italic ? "italic" : "normal",
                        }}
                      >
                        Aa
                      </span>
                      <span className="text-[8px] text-muted-foreground mt-1 truncate w-full text-center px-1">{font.name}</span>
                    </div>
                  ))}
                </div>
              )}

              {activeAsset === "palettes" && (
                <div className="flex flex-col gap-1.5">
                  {PALETTE_ASSETS.map((palette, i) => (
                    <div
                      key={i}
                      className="group rounded-lg border border-border/60 bg-card hover:border-primary/50 transition-all cursor-pointer overflow-hidden"
                      data-testid={`asset-palette-${i}`}
                    >
                      <div className="flex h-7">
                        {palette.colors.map((color, ci) => (
                          <div key={ci} className="flex-1 h-full" style={{ backgroundColor: color }} />
                        ))}
                      </div>
                      <div className="px-2 py-0.5">
                        <span className="text-[9px] text-muted-foreground">{palette.name}</span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

            </div>
          </div>
        )}
      </div>
    </div>
  );
}
