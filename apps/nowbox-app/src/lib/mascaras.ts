// Máscaras apenas visuais: os formulários continuam enviando somente os dígitos para a API.

function somenteDigitos(valor: string, limite: number) {
  return valor.replace(/\D/g, '').slice(0, limite)
}

export function mascaraCpf(valor = '') {
  return somenteDigitos(valor, 11)
    .replace(/^(\d{3})(\d)/, '$1.$2')
    .replace(/^(\d{3})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{3})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3-$4')
}

export function mascaraCnpj(valor = '') {
  return somenteDigitos(valor, 14)
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{2})\.(\d{3})\.(\d{3})(\d)/, '$1.$2.$3/$4')
    .replace(/^(\d{2})\.(\d{3})\.(\d{3})\/(\d{4})(\d)/, '$1.$2.$3/$4-$5')
}

export function mascaraCep(valor = '') {
  return somenteDigitos(valor, 8).replace(/^(\d{5})(\d)/, '$1-$2')
}

export function mascaraTelefone(valor = '') {
  const digitos = somenteDigitos(valor, 11)

  if (digitos.length <= 2) return digitos.replace(/^(\d+)/, '($1')
  if (digitos.length <= 6) return digitos.replace(/^(\d{2})(\d+)/, '($1) $2')
  if (digitos.length <= 10) return digitos.replace(/^(\d{2})(\d{4})(\d+)/, '($1) $2-$3')
  return digitos.replace(/^(\d{2})(\d{5})(\d+)/, '($1) $2-$3')
}

// O RG não tem formato nacional único: o padrão 00.000.000-0 vale para até 9 caracteres
// (o último pode ser "X") e valores maiores são mantidos sem pontuação.
export function mascaraRg(valor = '') {
  const alfanumerico = valor.replace(/[^0-9xX]/g, '').toUpperCase()

  if (alfanumerico.length > 9) return alfanumerico
  return alfanumerico
    .replace(/^(\d{2})(\d)/, '$1.$2')
    .replace(/^(\d{2})\.(\d{3})(\d)/, '$1.$2.$3')
    .replace(/^(\d{2})\.(\d{3})\.(\d{3})([\dX])/, '$1.$2.$3-$4')
}

export function semMascaraRg(valor: string) {
  return valor.replace(/[^0-9xX]/g, '').toUpperCase()
}
