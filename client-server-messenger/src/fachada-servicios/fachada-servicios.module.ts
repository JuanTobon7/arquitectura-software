import { Module } from '@nestjs/common';
import { FachadaDeServiciosService } from './fachada-de-servicios.service.js';

@Module({
  providers: [FachadaDeServiciosService]
})
export class FachadaServiciosModule {}
