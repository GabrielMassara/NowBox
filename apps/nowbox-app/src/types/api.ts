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
  createdAt?: string
  deletedAt?: string
}

export interface UsuarioCreateDTO {
  nome: string
  email: string
  cpf: string
  sexo: string
  senha: string
}

export interface EstadoEntity {
  id: string
  nome: string
  uf: string
}

export interface ClienteResponseDTO {
  id: string
  estado: EstadoEntity
  nome: string
  profissao: string
  cpf: string
  rg: string
  email: string
  telefone: string
  sexo: string
  nascimento: string
  endereco: string
  numero: string
  complemento?: string
  bairro: string
  cep: string
  cidade: string
  enderecoCorrespondencia?: boolean
  senhaTemporariaStatus?: boolean
  createdAt?: string
  deletedAt?: string
}

export interface ClienteCreateDTO {
  idEstado: string
  nome: string
  profissao: string
  cpf: string
  rg: string
  email: string
  telefone: string
  sexo: string
  nascimento: string
  endereco: string
  numero: string
  complemento?: string
  bairro: string
  cep: string
  cidade: string
  enderecoCorrespondencia: boolean
  senha: string
  senhaTemporariaStatus?: boolean
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
  usuario: UsuarioResponseDTO
  cargo: CargoEntity
  createdAt?: string
  deletedAt?: string
}

export interface AtribuicaoCreateDTO {
  idUsuario: string
  idCargo: string
}

export interface BoxResponseDTO {
  id: string
  unidade: UnidadeEntity
  numero: string
  tamanho: number
  dimensoes: string
  disponivel: boolean
  preco: number
  createdAt?: string
  deletedAt?: string
}

export interface BoxCreateDTO {
  idUnidade: string
  numero: string
  tamanho: number
  dimensoes: string
  disponivel: boolean
  preco: number
}

export interface BoxLoteDTO {
  idUnidade: string
  prefixo?: string
  numeroInicial: number
  numeroFinal: number
  completarComZeros: boolean
  tamanho: number
  dimensoes: string
  disponivel: boolean
  preco: number
}

export interface AluguelResponseDTO {
  id: string
  box: BoxResponseDTO
  cliente: ClienteResponseDTO
  valor: number
  observacao?: string
  status: boolean
  createdAt?: string
  deletedAt?: string
}

export interface AluguelCreateDTO {
  idBox: string
  idCliente: string
  valor: number
  observacao?: string
  status: boolean
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
