import { defineStore } from "pinia";

export const useStore = defineStore("store", {
  state: () => ({
    user: null as any,
    is_login: false,
  }),

})