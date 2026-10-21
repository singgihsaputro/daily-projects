import recipes from '../../mock/recipes.json'

// The only file that knows where recipes come from. A real API client can
// replace this as long as list() still resolves to [{ id, name, kcal, ingredients }].
export const recipeSource = {
  async list() {
    return recipes
  },
}
