import TemplateLibrary from './TemplateLibrary';
import AssetLibrary from './AssetLibrary';

export function LeftPanel() {
  return (
    <div className="w-full h-full bg-[var(--bg-base)] flex flex-col overflow-y-auto">
      <div className="flex flex-col gap-0">
        <TemplateLibrary />
        <AssetLibrary />
      </div>
    </div>
  );
}
