export interface LoginRequestDTO {
  email: string
  senha: string
}

export interface LoginResponseDTO {
  token: string
}

export interface UsuarioResponseDTO {
  id: string
  nome: string
  email: string
  cpf: string
  sexo: string
}

export interface EstadoEntity {
  id: string
  nome: string
  uf: string
}

export interface UnidadeEntity {
  id: string
  nome: string
  cnpj: string
  endereco: string
  numero: string
  complemento?: string
  bairro: string
  cep: string
  cidade: string
  estado: EstadoEntity
}

export interface UnidadeResponseDTO extends UnidadeEntity {
  createdAt?: string
  deletedAt?: string
}

export interface UnidadeCreateDTO {
  idEstado: string
  nome: string
  cnpj: string
  endereco: string
  numero: string
  complemento?: string
  bairro: string
  cep: string
  cidade: string
}

export interface CargoEntity {
  id: string
  nome: string
  unidade: UnidadeEntity
}

export interface AtribuicaoResponseDTO {
  id: string
  usuario: { id: string; nome: string; email: string }
  cargo: CargoEntity
}

export interface PageResponse<T> {
  totalPages: number
  totalElements: number
  size: number
  content: T[]
  number: number
}

export interface MenuModuloResponseDTO {
  id: string
  nome: string
  rota: string
}

export interface MenuSessaoResponseDTO {
  id: string
  nome: string
  rota: string
  modulos: MenuModuloResponseDTO[]
}

export interface SessaoResponseDTO {
  id: string
  nome: string
  rota: string
}

export interface SessaoCreateDTO {
  nome: string
  rota: string
}

export interface ModuloResponseDTO {
  id: string
  sessao: SessaoResponseDTO
  nome: string
  rota: string
}

export interface ModuloCreateDTO {
  idSessao: string
  nome: string
  rota: string
}

export interface OperacaoResponseDTO {
  id: string
  modulo: ModuloResponseDTO
  nome: string
  codigo: string
}

export interface OperacaoCreateDTO {
  idModulo: string
  nome: string
  codigo: string
}

export interface CargoResponseDTO {
  id: string
  nome: string
  unidade: UnidadeEntity
}

export interface CargoCreateDTO {
  idUnidade: string
  nome: string
}

export interface PermissaoResponseDTO {
  id: string
  cargo: CargoEntity
  operacao: OperacaoResponseDTO
}

export interface PermissaoCreateDTO {
  idCargo: string
  idOperacao: string
}

export interface PermissaoLoteDTO {
  idsOperacao: string[]
}

export interface PageMetadata {
  size: number
  number: number
  totalElements: number
  totalPages: number
}

export interface PagedResponse<T> {
  content: T[]
  page: PageMetadata
}
