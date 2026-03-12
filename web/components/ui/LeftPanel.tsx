import TemplateLibrary from './TemplateLibrary';
import AssetLibrary from './AssetLibrary';

export function LeftPanel() {
  return (
    <div className="w-[200px] h-full bg-[var(--bg-base)] py-4 px-3 flex flex-col shrink-0 overflow-y-auto animate-fade-in-left">
      <TemplateLibrary />
      <div className="h-[1px] bg-[var(--bg-border)] mb-4 mx-2" />
      <AssetLibrary />
    </div>
  );
}
