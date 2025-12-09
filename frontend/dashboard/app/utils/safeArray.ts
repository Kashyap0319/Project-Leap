export const safeArray = (value: any): any[] => {
  return Array.isArray(value) ? value : [];
};
