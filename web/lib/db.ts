import Dexie, { type EntityTable } from 'dexie';

export interface Project {
  id?: number;
  name: string;
  prompt: string;
  code: string;
  videoBlob?: Blob;
  createdAt: number;
  updatedAt: number;
  template: string | null;
  scenes: any[];
}

export interface Asset {
  id?: number;
  projectId: number;
  type: string;
  data: any;
}

const db = new Dexie('ManimStudioDB') as Dexie & {
  projects: EntityTable<Project, 'id'>;
  assets: EntityTable<Asset, 'id'>;
};

// Schema declaration
db.version(1).stores({
  projects: '++id, name, createdAt, updatedAt',
  assets: '++id, projectId, type'
});

export { db };
